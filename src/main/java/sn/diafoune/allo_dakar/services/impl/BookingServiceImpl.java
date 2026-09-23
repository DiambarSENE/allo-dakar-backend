package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.commons.utils.ReferenceGenerator;
import sn.diafoune.allo_dakar.entities.AuditLog;
import sn.diafoune.allo_dakar.entities.Booking;
import sn.diafoune.allo_dakar.entities.Trip;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.BookingStatus;
import sn.diafoune.allo_dakar.entities.enums.NotificationType;
import sn.diafoune.allo_dakar.entities.enums.PaymentStatus;
import sn.diafoune.allo_dakar.entities.enums.UserStatus;
import sn.diafoune.allo_dakar.exceptions.BadRequestException;
import sn.diafoune.allo_dakar.exceptions.BookingException;
import sn.diafoune.allo_dakar.exceptions.ErrorCode;
import sn.diafoune.allo_dakar.exceptions.ForbiddenException;
import sn.diafoune.allo_dakar.exceptions.ResourceNotFoundException;
import sn.diafoune.allo_dakar.mappers.BookingMapper;
import sn.diafoune.allo_dakar.repositories.AuditLogRepository;
import sn.diafoune.allo_dakar.repositories.BookingRepository;
import sn.diafoune.allo_dakar.repositories.UserRepository;
import sn.diafoune.allo_dakar.services.interfaces.BookingService;
import sn.diafoune.allo_dakar.services.interfaces.NotificationService;
import sn.diafoune.allo_dakar.services.interfaces.PassengerService;
import sn.diafoune.allo_dakar.services.interfaces.UserService;
import sn.diafoune.allo_dakar.web.dtos.booking.BookingResponse;
import sn.diafoune.allo_dakar.web.dtos.booking.CreateBookingRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Voir SeatReservationService pour la stratégie de concurrence détaillée. Ici : orchestration
 * métier + retry borné (3 tentatives) sur conflit de version avant d'abandonner (409 au client,
 * qui peut réessayer — cas normal en cas de forte contention, pas une erreur applicative).
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final BookingRepository bookingRepository;
    private final SeatReservationService seatReservationService;
    private final UserService userService;
    private final PassengerService passengerService;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    public BookingResponse create(UUID passengerUserId, CreateBookingRequest request) {
        User passenger = userService.getEntityById(passengerUserId);
        if (passenger.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("Ce compte n'est pas autorisé à réserver (statut : " + passenger.getStatus() + ")");
        }
        passengerService.getOrCreateForUser(passenger);

        if (bookingRepository.existsByTripIdAndPassengerIdAndStatusIn(
                request.tripId(), passengerUserId, List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED))) {
            throw new BookingException(ErrorCode.CONFLICT,
                    "Une réservation active existe déjà pour ce trajet");
        }

        Trip reservedTrip = reserveSeatsWithRetry(request.tripId(), request.numberOfSeats());

        BigDecimal totalAmount = reservedTrip.getPricePerSeat().multiply(BigDecimal.valueOf(request.numberOfSeats()));

        Booking booking = Booking.builder()
                .bookingReference(ReferenceGenerator.bookingReference())
                .trip(reservedTrip)
                .passenger(passenger)
                .numberOfSeats(request.numberOfSeats())
                .unitPrice(reservedTrip.getPricePerSeat())
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .bookedAt(Instant.now())
                .build();

        booking = bookingRepository.save(booking);
        log.info("BOOKING_CREATED bookingId={} tripId={} passengerId={}", booking.getId(), reservedTrip.getId(), passengerUserId);

        notificationService.notify(passenger, NotificationType.BOOKING_CREATED,
                "Réservation créée", "Votre réservation " + booking.getBookingReference() + " est en attente de paiement.");
        notificationService.notify(reservedTrip.getDriver().getUser(), NotificationType.BOOKING_CREATED,
                "Nouvelle réservation", booking.getNumberOfSeats() + " place(s) réservée(s) sur votre trajet.");

        return bookingMapper.toResponse(booking);
    }

    /**
     * Chaque tentative s'exécute dans sa propre transaction (REQUIRES_NEW, voir
     * SeatReservationService) : un conflit de version n'invalide donc pas le persistence context
     * de cette méthode, qui peut simplement relire l'état courant et retenter.
     */
    private Trip reserveSeatsWithRetry(UUID tripId, int seats) {
        ObjectOptimisticLockingFailureException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return seatReservationService.reserveSeats(tripId, seats);
            } catch (ObjectOptimisticLockingFailureException ex) {
                lastFailure = ex;
                log.warn("Conflit de version sur Trip {} (tentative {}/{})", tripId, attempt, MAX_RETRY_ATTEMPTS);
            }
        }
        throw lastFailure;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getById(UUID bookingId, UUID requestingUserId) {
        Booking booking = getEntity(bookingId);
        boolean isPassenger = booking.getPassenger().getId().equals(requestingUserId);
        boolean isDriver = booking.getTrip().getDriver().getUser().getId().equals(requestingUserId);
        if (!isPassenger && !isDriver) {
            throw new ForbiddenException("Vous n'avez pas accès à cette réservation");
        }
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getByIdForStaff(UUID bookingId) {
        return bookingMapper.toResponse(getEntity(bookingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> listMine(UUID passengerUserId, Pageable pageable) {
        return bookingRepository.findByPassengerId(passengerUserId, pageable).map(bookingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> listForDriver(UUID driverUserId, Pageable pageable) {
        return bookingRepository.findByTripDriverUserId(driverUserId, pageable).map(bookingMapper::toResponse);
    }

    @Override
    @Transactional
    public BookingResponse confirmAfterPayment(UUID bookingId) {
        Booking booking = getEntity(bookingId);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.SUCCESS);
        booking.setConfirmedAt(Instant.now());
        booking = bookingRepository.save(booking);

        log.info("BOOKING_CONFIRMED bookingId={}", booking.getId());
        notificationService.notify(booking.getPassenger(), NotificationType.BOOKING_CONFIRMED,
                "Réservation confirmée", "Votre réservation " + booking.getBookingReference() + " est confirmée.");

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancel(UUID bookingId, UUID requestingUserId, String reason) {
        Booking booking = getEntity(bookingId);
        boolean isPassenger = booking.getPassenger().getId().equals(requestingUserId);
        boolean isDriver = booking.getTrip().getDriver().getUser().getId().equals(requestingUserId);
        if (!isPassenger && !isDriver) {
            throw new ForbiddenException("Vous n'avez pas le droit d'annuler cette réservation");
        }
        return doCancel(booking, reason);
    }

    @Override
    @Transactional
    public BookingResponse cancelOnBehalf(UUID bookingId, UUID staffUserId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Un motif est requis pour annuler une réservation pour le compte d'un utilisateur");
        }
        Booking booking = getEntity(bookingId);
        BookingResponse response = doCancel(booking, reason);

        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", staffUserId));
        auditLogRepository.save(AuditLog.builder()
                .actor(staff)
                .entityType("Booking")
                .entityId(booking.getId())
                .action("BOOKING_CANCELLED_BY_STAFF")
                .details(reason)
                .build());
        log.info("BOOKING_CANCELLED_BY_STAFF bookingId={} staffUserId={} reason={}", booking.getId(), staffUserId, reason);

        return response;
    }

    /** Logique de changement de statut partagée entre cancel() et cancelOnBehalf() — seule l'autorisation diffère entre les deux. */
    private BookingResponse doCancel(Booking booking, String reason) {
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException(ErrorCode.CONFLICT, "Cette réservation ne peut plus être annulée");
        }

        // Règle §48-9 : libérer les places uniquement si elles avaient bien été décomptées
        // (PENDING ou CONFIRMED — pas REJECTED, qui n'a jamais consommé de places).
        boolean shouldReleaseSeats = booking.getStatus() == BookingStatus.PENDING
                || booking.getStatus() == BookingStatus.CONFIRMED;

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        booking.setCancellationReason(reason);
        booking = bookingRepository.save(booking);

        if (shouldReleaseSeats) {
            seatReservationService.releaseSeats(booking.getTrip().getId(), booking.getNumberOfSeats());
        }

        log.info("BOOKING_CANCELLED bookingId={} reason={}", booking.getId(), reason);
        notificationService.notify(booking.getPassenger(), NotificationType.BOOKING_CANCELLED,
                "Réservation annulée", "Votre réservation " + booking.getBookingReference() + " a été annulée.");

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public void markCompleted(UUID bookingId) {
        Booking booking = getEntity(bookingId);
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(Instant.now());
        bookingRepository.save(booking);
    }

    private Booking getEntity(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", bookingId));
    }
}
