package sn.diafoune.allo_dakar.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import sn.diafoune.allo_dakar.entities.DriverProfile;
import sn.diafoune.allo_dakar.entities.Role;
import sn.diafoune.allo_dakar.entities.Trip;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.Vehicle;
import sn.diafoune.allo_dakar.entities.enums.DriverVerificationStatus;
import sn.diafoune.allo_dakar.entities.enums.RoleType;
import sn.diafoune.allo_dakar.entities.enums.TripStatus;
import sn.diafoune.allo_dakar.entities.enums.UserStatus;
import sn.diafoune.allo_dakar.entities.enums.VehicleType;
import sn.diafoune.allo_dakar.exceptions.BookingException;
import sn.diafoune.allo_dakar.repositories.DriverProfileRepository;
import sn.diafoune.allo_dakar.repositories.RoleRepository;
import sn.diafoune.allo_dakar.repositories.TripRepository;
import sn.diafoune.allo_dakar.repositories.UserRepository;
import sn.diafoune.allo_dakar.repositories.VehicleRepository;
import sn.diafoune.allo_dakar.services.interfaces.BookingService;
import sn.diafoune.allo_dakar.web.dtos.booking.CreateBookingRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reproduit le scénario du §45 : 5 places disponibles, deux utilisateurs réservent 3 places
 * chacun simultanément (demande totale = 6 > 5). Un seul doit réussir ; l'autre doit échouer
 * avec BookingException(BOOKING_SEATS_UNAVAILABLE) après épuisement du retry optimiste (voir
 * SeatReservationService/BookingServiceImpl). Le nombre de places restantes ne doit jamais
 * devenir négatif.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class BookingConcurrencyIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("allo_dakar_test")
            .withUsername("allo_dakar_test")
            .withPassword("allo_dakar_test");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private BookingService bookingService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private DriverProfileRepository driverProfileRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private TripRepository tripRepository;

    @Test
    void deuxReservationsConcurrentesNeDepassentJamaisLaCapacite() throws Exception {
        Role passengerRole = roleRepository.findByName(RoleType.PASSENGER).orElseThrow();

        User driverUser = userRepository.save(User.builder()
                .firstName("Ousmane").lastName("Ba").email("driver-" + System.nanoTime() + "@allodakar.sn")
                .phone("77" + System.nanoTime() % 10_000_000).keycloakId(java.util.UUID.randomUUID().toString())
                .status(UserStatus.ACTIVE).roles(java.util.Set.of(passengerRole)).build());

        DriverProfile driver = driverProfileRepository.save(DriverProfile.builder()
                .user(driverUser).licenseNumber("LIC-" + System.nanoTime())
                .licenseExpiryDate(LocalDate.now().plusYears(2))
                .verificationStatus(DriverVerificationStatus.VERIFIED).build());

        Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                .driver(driver).brand("Toyota").model("Corolla")
                .registrationNumber("DK-" + System.nanoTime() + "-A").numberOfSeats(5)
                .vehicleType(VehicleType.SEDAN).build());

        Trip trip = tripRepository.save(Trip.builder()
                .driver(driver).vehicle(vehicle)
                .departureCity("Dakar").destinationCity("Thiès")
                .departureDate(LocalDate.now().plusDays(3)).departureTime(LocalTime.of(9, 0))
                .pricePerSeat(BigDecimal.valueOf(3000))
                .totalSeats(5).availableSeats(5)
                .status(TripStatus.PUBLISHED).build());

        User passengerA = userRepository.save(User.builder()
                .firstName("Awa").lastName("Ndiaye").email("pax-a-" + System.nanoTime() + "@allodakar.sn")
                .phone("78" + System.nanoTime() % 10_000_000).keycloakId(java.util.UUID.randomUUID().toString())
                .status(UserStatus.ACTIVE).roles(java.util.Set.of(passengerRole)).build());
        User passengerB = userRepository.save(User.builder()
                .firstName("Ibra").lastName("Sow").email("pax-b-" + System.nanoTime() + "@allodakar.sn")
                .phone("76" + System.nanoTime() % 10_000_000).keycloakId(java.util.UUID.randomUUID().toString())
                .status(UserStatus.ACTIVE).roles(java.util.Set.of(passengerRole)).build());

        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Boolean> resultA = executor.submit(makeAttempt(passengerA.getId(), trip.getId(), readyLatch, startLatch));
        Future<Boolean> resultB = executor.submit(makeAttempt(passengerB.getId(), trip.getId(), readyLatch, startLatch));

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();

        boolean successA = resultA.get(10, TimeUnit.SECONDS);
        boolean successB = resultB.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Demande totale = 6 places pour 5 disponibles : exactement une des deux doit réussir.
        assertThat(successA ^ successB).as("exactement une des deux réservations doit réussir").isTrue();

        Trip refreshed = tripRepository.findById(trip.getId()).orElseThrow();
        assertThat(refreshed.getAvailableSeats()).isGreaterThanOrEqualTo(0);
        assertThat(refreshed.getAvailableSeats()).isEqualTo(2); // 5 - 3 (une seule réservation a été acceptée)
    }

    private Callable<Boolean> makeAttempt(java.util.UUID passengerId, java.util.UUID tripId,
                                            CountDownLatch readyLatch, CountDownLatch startLatch) {
        return () -> {
            readyLatch.countDown();
            startLatch.await();
            try {
                bookingService.create(passengerId, new CreateBookingRequest(tripId, 3));
                return true;
            } catch (BookingException ex) {
                return false;
            }
        };
    }
}
