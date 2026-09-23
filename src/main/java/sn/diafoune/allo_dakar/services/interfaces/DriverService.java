package sn.diafoune.allo_dakar.services.interfaces;

import sn.diafoune.allo_dakar.entities.DriverProfile;
import sn.diafoune.allo_dakar.web.dtos.driver.DriverProfileResponse;
import sn.diafoune.allo_dakar.web.dtos.driver.UpdateDriverProfileRequest;

import java.util.UUID;

public interface DriverService {

    DriverProfile getOrCreateForUser(UUID userId);

    DriverProfile getEntityById(UUID driverProfileId);

    DriverProfileResponse getById(UUID driverProfileId);

    /** Auto-provisionne le profil conducteur au premier accès si nécessaire (voir getOrCreateForUser). */
    DriverProfileResponse getMyProfile(UUID userId);

    DriverProfileResponse updateMe(UUID userId, UpdateDriverProfileRequest request);

    void recalculateAfterCompletedTrip(UUID driverProfileId);
}
