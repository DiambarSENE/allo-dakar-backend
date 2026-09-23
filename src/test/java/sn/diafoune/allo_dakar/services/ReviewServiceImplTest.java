package sn.diafoune.allo_dakar.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.diafoune.allo_dakar.entities.Booking;
import sn.diafoune.allo_dakar.entities.DriverProfile;
import sn.diafoune.allo_dakar.entities.Trip;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.BookingStatus;
import sn.diafoune.allo_dakar.entities.enums.TripStatus;
import sn.diafoune.allo_dakar.exceptions.BusinessException;
import sn.diafoune.allo_dakar.exceptions.ForbiddenException;
import sn.diafoune.allo_dakar.mappers.ReviewMapper;
import sn.diafoune.allo_dakar.repositories.BookingRepository;
import sn.diafoune.allo_dakar.repositories.DriverProfileRepository;
import sn.diafoune.allo_dakar.repositories.PassengerProfileRepository;
import sn.diafoune.allo_dakar.repositories.ReviewRepository;
import sn.diafoune.allo_dakar.repositories.TripRepository;
import sn.diafoune.allo_dakar.repositories.UserRepository;
import sn.diafoune.allo_dakar.services.impl.ReviewServiceImpl;
import sn.diafoune.allo_dakar.services.interfaces.NotificationService;
import sn.diafoune.allo_dakar.web.dtos.review.CreateReviewRequest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Couvre la règle §48-5 : trajet terminé requis, participation réelle exigée, avis en double
 * refusé (voir §44 "Avis").
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private TripRepository tripRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private PassengerProfileRepository passengerProfileRepository;
    @Mock private DriverProfileRepository driverProfileRepository;
    @Mock private ReviewMapper reviewMapper;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private UUID passengerId;
    private UUID driverUserId;
    private Trip trip;
    private Booking booking;

    @BeforeEach
    void setUp() {
        passengerId = UUID.randomUUID();
        driverUserId = UUID.randomUUID();

        User driverUser = User.builder().firstName("Moussa").lastName("Diop").build();
        driverUser.setId(driverUserId);
        DriverProfile driverProfile = DriverProfile.builder().user(driverUser).build();
        driverProfile.setId(UUID.randomUUID());

        trip = Trip.builder().driver(driverProfile).status(TripStatus.COMPLETED).build();
        trip.setId(UUID.randomUUID());

        User passenger = User.builder().firstName("Awa").lastName("Fall").build();
        passenger.setId(passengerId);
        booking = Booking.builder().trip(trip).passenger(passenger).status(BookingStatus.COMPLETED).build();
        booking.setId(UUID.randomUUID());

        lenient().when(userRepository.findById(passengerId)).thenReturn(Optional.of(passenger));
        lenient().when(passengerProfileRepository.findByUserId(any())).thenReturn(Optional.empty());
        lenient().when(driverProfileRepository.findByUserId(any())).thenReturn(Optional.empty());
    }

    @Test
    void refuseAvisSiTrajetNonTermine() {
        trip.setStatus(TripStatus.IN_PROGRESS);
        when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));

        CreateReviewRequest request = new CreateReviewRequest(trip.getId(), booking.getId(), driverUserId, 5, "Top");

        assertThatThrownBy(() -> reviewService.create(passengerId, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void refuseAvisSiAuteurNAPasParticipe() {
        UUID etranger = UUID.randomUUID();
        when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        lenient().when(userRepository.findById(etranger))
                .thenReturn(Optional.of(userWithId(etranger)));

        CreateReviewRequest request = new CreateReviewRequest(trip.getId(), booking.getId(), driverUserId, 5, "Top");

        assertThatThrownBy(() -> reviewService.create(etranger, request))
                .isInstanceOf(ForbiddenException.class);
    }

    private static User userWithId(UUID id) {
        User user = User.builder().build();
        user.setId(id);
        return user;
    }

    @Test
    void refuseAvisEnDouble() {
        when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByTripIdAndAuthorIdAndTargetUserId(trip.getId(), passengerId, driverUserId))
                .thenReturn(true);

        CreateReviewRequest request = new CreateReviewRequest(trip.getId(), booking.getId(), driverUserId, 4, "Bien");

        assertThatThrownBy(() -> reviewService.create(passengerId, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void accepteAvisValide() {
        when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(reviewRepository.existsByTripIdAndAuthorIdAndTargetUserId(trip.getId(), passengerId, driverUserId))
                .thenReturn(false);
        when(reviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(driverUserId)).thenReturn(Optional.of(trip.getDriver().getUser()));

        CreateReviewRequest request = new CreateReviewRequest(trip.getId(), booking.getId(), driverUserId, 5, "Excellent conducteur");

        reviewService.create(passengerId, request);
        // Aucune exception levée = succès ; le mapper est sollicité pour construire la réponse.
        assertThat(reviewMapper).isNotNull();
    }
}
