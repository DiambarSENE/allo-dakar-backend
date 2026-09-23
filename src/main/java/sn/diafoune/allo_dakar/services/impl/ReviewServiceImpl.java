package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.entities.Booking;
import sn.diafoune.allo_dakar.entities.Review;
import sn.diafoune.allo_dakar.entities.Trip;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.BookingStatus;
import sn.diafoune.allo_dakar.entities.enums.NotificationType;
import sn.diafoune.allo_dakar.entities.enums.ReviewStatus;
import sn.diafoune.allo_dakar.entities.enums.TripStatus;
import sn.diafoune.allo_dakar.exceptions.BadRequestException;
import sn.diafoune.allo_dakar.exceptions.BusinessException;
import sn.diafoune.allo_dakar.exceptions.ErrorCode;
import sn.diafoune.allo_dakar.exceptions.ForbiddenException;
import sn.diafoune.allo_dakar.exceptions.ResourceNotFoundException;
import sn.diafoune.allo_dakar.mappers.ReviewMapper;
import sn.diafoune.allo_dakar.repositories.BookingRepository;
import sn.diafoune.allo_dakar.repositories.DriverProfileRepository;
import sn.diafoune.allo_dakar.repositories.PassengerProfileRepository;
import sn.diafoune.allo_dakar.repositories.ReviewRepository;
import sn.diafoune.allo_dakar.repositories.TripRepository;
import sn.diafoune.allo_dakar.repositories.UserRepository;
import sn.diafoune.allo_dakar.services.interfaces.NotificationService;
import sn.diafoune.allo_dakar.services.interfaces.ReviewService;
import sn.diafoune.allo_dakar.web.dtos.review.CreateReviewRequest;
import sn.diafoune.allo_dakar.web.dtos.review.ReviewResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Règle §48-5 : un avis n'est possible qu'après Trip.status = COMPLETED, et seul un participant
 * réel (le conducteur ou un passager dont la réservation est COMPLETED) peut noter l'autre
 * partie. Contrainte unique (trip, author, target) empêchant les doublons — voir migration V10.
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PassengerProfileRepository passengerProfileRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final ReviewMapper reviewMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ReviewResponse create(UUID authorId, CreateReviewRequest request) {
        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> ResourceNotFoundException.of("Trip", request.tripId()));
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED,
                    "Un avis ne peut être laissé que sur un trajet terminé");
        }

        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", request.bookingId()));
        if (!booking.getTrip().getId().equals(trip.getId())) {
            throw new BadRequestException("Cette réservation ne correspond pas au trajet indiqué");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", authorId));

        User expectedTarget = resolveExpectedTarget(trip, booking, authorId);

        if (!expectedTarget.getId().equals(request.targetUserId())) {
            throw new BadRequestException("La cible de l'avis ne correspond pas à votre rôle dans ce trajet");
        }
        if (reviewRepository.existsByTripIdAndAuthorIdAndTargetUserId(trip.getId(), authorId, request.targetUserId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW,
                    "Un avis a déjà été laissé pour ce trajet et cette cible");
        }

        Review review = Review.builder()
                .trip(trip)
                .booking(booking)
                .author(author)
                .targetUser(expectedTarget)
                .rating(request.rating())
                .comment(request.comment())
                .status(ReviewStatus.PUBLISHED)
                .build();
        review = reviewRepository.save(review);

        recalculateRatings(expectedTarget.getId());
        notificationService.notify(expectedTarget, NotificationType.REVIEW_RECEIVED,
                "Nouvel avis reçu", "Vous avez reçu un nouvel avis suite à un trajet.");

        return reviewMapper.toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> listForUser(UUID targetUserId, Pageable pageable) {
        return reviewRepository.findByTargetUserIdAndStatus(targetUserId, ReviewStatus.PUBLISHED, pageable)
                .map(reviewMapper::toResponse);
    }

    @Override
    @Transactional
    public ReviewResponse update(UUID authorId, UUID reviewId, String comment, Integer rating) {
        Review review = getEntity(reviewId);
        if (!review.getAuthor().getId().equals(authorId)) {
            throw new ForbiddenException("Seul l'auteur de l'avis peut le modifier");
        }
        if (rating != null) {
            if (rating < 1 || rating > 5) {
                throw new BadRequestException("La note doit être comprise entre 1 et 5");
            }
            review.setRating(rating);
        }
        if (comment != null) {
            review.setComment(comment);
        }
        reviewRepository.save(review);
        recalculateRatings(review.getTargetUser().getId());
        return reviewMapper.toResponse(review);
    }

    @Override
    @Transactional
    public void delete(UUID authorId, UUID reviewId) {
        Review review = getEntity(reviewId);
        if (!review.getAuthor().getId().equals(authorId)) {
            throw new ForbiddenException("Seul l'auteur de l'avis peut le supprimer");
        }
        UUID targetId = review.getTargetUser().getId();
        reviewRepository.delete(review);
        recalculateRatings(targetId);
    }

    @Override
    @Transactional
    public void moderate(UUID reviewId, UUID moderatorId, boolean hide) {
        Review review = getEntity(reviewId);
        review.setStatus(hide ? ReviewStatus.HIDDEN : ReviewStatus.PUBLISHED);
        reviewRepository.save(review);
        recalculateRatings(review.getTargetUser().getId());
    }

    /**
     * Détermine qui l'auteur est autorisé à noter : le conducteur ne peut noter que le passager
     * de la réservation fournie, et un passager ne peut noter que le conducteur du trajet — et
     * uniquement si sa propre réservation est COMPLETED.
     */
    private User resolveExpectedTarget(Trip trip, Booking booking, UUID authorId) {
        boolean authorIsDriver = trip.getDriver().getUser().getId().equals(authorId);
        if (authorIsDriver) {
            return booking.getPassenger();
        }
        boolean authorIsPassenger = booking.getPassenger().getId().equals(authorId);
        if (authorIsPassenger && booking.getStatus() == BookingStatus.COMPLETED) {
            return trip.getDriver().getUser();
        }
        throw new ForbiddenException("Vous n'avez pas participé à ce trajet, ou votre réservation n'est pas terminée");
    }

    private void recalculateRatings(UUID targetUserId) {
        Double avg = reviewRepository.computeAverageRating(targetUserId);
        BigDecimal rounded = avg == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);

        passengerProfileRepository.findByUserId(targetUserId)
                .ifPresent(p -> {
                    p.setAverageRating(rounded);
                    passengerProfileRepository.save(p);
                });

        driverProfileRepository.findByUserId(targetUserId)
                .ifPresent(d -> {
                    d.setAverageRating(rounded);
                    d.setTotalReviews((int) reviewRepository
                            .findByTargetUserIdAndStatus(targetUserId, ReviewStatus.PUBLISHED,
                                    org.springframework.data.domain.Pageable.ofSize(1))
                            .getTotalElements());
                    driverProfileRepository.save(d);
                });
    }

    private Review getEntity(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Review", id));
    }
}
