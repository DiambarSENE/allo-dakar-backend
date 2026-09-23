package sn.diafoune.allo_dakar.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.diafoune.allo_dakar.commons.configs.BusinessRuleProperties;
import sn.diafoune.allo_dakar.entities.DriverProfile;
import sn.diafoune.allo_dakar.entities.Trip;
import sn.diafoune.allo_dakar.entities.Vehicle;
import sn.diafoune.allo_dakar.entities.enums.DriverVerificationStatus;
import sn.diafoune.allo_dakar.entities.enums.TripStatus;
import sn.diafoune.allo_dakar.exceptions.BusinessException;
import sn.diafoune.allo_dakar.mappers.TripMapper;
import sn.diafoune.allo_dakar.repositories.TripRepository;
import sn.diafoune.allo_dakar.services.impl.TripServiceImpl;
import sn.diafoune.allo_dakar.services.interfaces.DriverService;
import sn.diafoune.allo_dakar.services.interfaces.VehicleService;
import sn.diafoune.allo_dakar.web.dtos.trip.TripResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Règle §48-1 : un conducteur non vérifié ne peut pas publier de trajet quand
 * app.business.driver-verification-required-to-publish=true (comportement par défaut).
 */
@ExtendWith(MockitoExtension.class)
class TripServiceImplTest {

    @Mock private TripRepository tripRepository;
    @Mock private DriverService driverService;
    @Mock private VehicleService vehicleService;
    @Mock private TripMapper tripMapper;

    private UUID driverUserId;
    private DriverProfile driverProfile;
    private Trip draftTrip;

    private TripServiceImpl buildService(boolean verificationRequired) {
        BusinessRuleProperties props = new BusinessRuleProperties(verificationRequired);
        return new TripServiceImpl(tripRepository, driverService, vehicleService, tripMapper, props);
    }

    private void setUpCommon() {
        driverUserId = UUID.randomUUID();
        driverProfile = DriverProfile.builder().build();
        driverProfile.setId(UUID.randomUUID());

        Vehicle vehicle = Vehicle.builder().driver(driverProfile).numberOfSeats(4).build();
        vehicle.setId(UUID.randomUUID());

        draftTrip = Trip.builder()
                .driver(driverProfile)
                .vehicle(vehicle)
                .status(TripStatus.DRAFT)
                .departureDate(LocalDate.now().plusDays(2))
                .departureTime(LocalTime.of(8, 0))
                .pricePerSeat(BigDecimal.TEN)
                .totalSeats(4)
                .availableSeats(4)
                .build();
        draftTrip.setId(UUID.randomUUID());

        lenient().when(driverService.getOrCreateForUser(driverUserId)).thenReturn(driverProfile);
        lenient().when(tripRepository.findById(draftTrip.getId())).thenReturn(java.util.Optional.of(draftTrip));
        lenient().when(tripRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(tripMapper.toResponse(any())).thenReturn(
                new TripResponse(null, null, null, null, null, null, null, null, null, null, null, null, 4, 4, null, null, TripStatus.PUBLISHED, null));
    }

    @Test
    void refusePublicationSiConducteurNonVerifie() {
        setUpCommon();
        driverProfile.setVerificationStatus(DriverVerificationStatus.PENDING);
        TripServiceImpl service = buildService(true);

        assertThatThrownBy(() -> service.publish(driverUserId, draftTrip.getId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void autorisePublicationSiConducteurVerifie() {
        setUpCommon();
        driverProfile.setVerificationStatus(DriverVerificationStatus.VERIFIED);
        TripServiceImpl service = buildService(true);

        assertThatCode(() -> service.publish(driverUserId, draftTrip.getId())).doesNotThrowAnyException();
    }

    @Test
    void autorisePublicationSiVerificationDesactiveeEnConfig() {
        setUpCommon();
        driverProfile.setVerificationStatus(DriverVerificationStatus.PENDING);
        TripServiceImpl service = buildService(false);

        assertThatCode(() -> service.publish(driverUserId, draftTrip.getId())).doesNotThrowAnyException();
    }
}
