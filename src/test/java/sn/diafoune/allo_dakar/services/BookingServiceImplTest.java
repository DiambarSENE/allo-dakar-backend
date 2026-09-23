package sn.diafoune.allo_dakar.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.diafoune.allo_dakar.entities.DriverProfile;
import sn.diafoune.allo_dakar.entities.Trip;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.TripStatus;
import sn.diafoune.allo_dakar.entities.enums.UserStatus;
import sn.diafoune.allo_dakar.exceptions.BookingException;
import sn.diafoune.allo_dakar.exceptions.ForbiddenException;
import sn.diafoune.allo_dakar.mappers.BookingMapper;
import sn.diafoune.allo_dakar.repositories.BookingRepository;
import sn.diafoune.allo_dakar.repositories.TripRepository;
import sn.diafoune.allo_dakar.services.impl.BookingServiceImpl;
import sn.diafoune.allo_dakar.services.impl.SeatReservationService;
import sn.diafoune.allo_dakar.services.interfaces.NotificationService;
import sn.diafoune.allo_dakar.services.interfaces.PassengerService;
import sn.diafoune.allo_dakar.services.interfaces.UserService;
import sn.diafoune.allo_dakar.web.dtos.booking.CreateBookingRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Couvre §44 "Réservation" : places disponibles -> SUCCESS, places insuffisantes -> ERROR,
 * trajet non publié -> ERROR. La stratégie de concurrence elle-même (verrouillage optimiste +
 * retry) est couverte par le test d'intégration BookingConcurrencyIntegrationTest.
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private TripRepository tripRepository;
    @Mock private UserService userService;
    @Mock private PassengerService passengerService;
    @Mock private BookingMapper bookingMapper;
    @Mock private NotificationService notificationService;

    private SeatReservationService seatReservationService;
    private BookingServiceImpl bookingService;

    private UUID passengerId;
    private Trip trip;
    private User passenger;

    @BeforeEach
    void setUp() {
        passengerId = UUID.randomUUID();
        DriverProfile driverProfile = DriverProfile.builder().build();
        driverProfile.setId(UUID.randomUUID());
        User driverUser = User.builder().build();
        driverUser.setId(UUID.randomUUID());
        driverProfile.setUser(driverUser);

        trip = Trip.builder()
                .driver(driverProfile)
                .status(TripStatus.PUBLISHED)
                .pricePerSeat(BigDecimal.valueOf(2500))
                .totalSeats(4)
                .availableSeats(2)
                .build();
        trip.setId(UUID.randomUUID());

        passenger = User.builder().status(UserStatus.ACTIVE).build();
        passenger.setId(passengerId);

        seatReservationService = new SeatReservationService(tripRepository);
//        bookingService = new BookingServiceImpl(bookingRepository, seatReservationService,
//                userService, passengerService, bookingMapper, notificationService);

        lenient().when(userService.getEntityById(passengerId)).thenReturn(passenger);
        lenient().when(tripRepository.findById(trip.getId())).thenReturn(Optional.of(trip));
        lenient().when(tripRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(bookingRepository.existsByTripIdAndPassengerIdAndStatusIn(any(), any(), any()))
                .thenReturn(false);
    }

    @Test
    void reservationReussieSiPlacesDisponibles() {
        CreateBookingRequest request = new CreateBookingRequest(trip.getId(), 2);

        assertThatCode(() -> bookingService.create(passengerId, request)).doesNotThrowAnyException();
    }

    @Test
    void reservationEchoueSiPlacesInsuffisantes() {
        CreateBookingRequest request = new CreateBookingRequest(trip.getId(), 3);

        assertThatThrownBy(() -> bookingService.create(passengerId, request))
                .isInstanceOf(BookingException.class);
    }

    @Test
    void reservationEchoueSiTrajetNonPublie() {
        trip.setStatus(TripStatus.CANCELLED);
        CreateBookingRequest request = new CreateBookingRequest(trip.getId(), 1);

        assertThatThrownBy(() -> bookingService.create(passengerId, request))
                .isInstanceOf(BookingException.class);
    }

    @Test
    void reservationEchoueSiCompteNonActif() {
        passenger.setStatus(UserStatus.SUSPENDED);
        CreateBookingRequest request = new CreateBookingRequest(trip.getId(), 1);

        assertThatThrownBy(() -> bookingService.create(passengerId, request))
                .isInstanceOf(ForbiddenException.class);
    }
}
