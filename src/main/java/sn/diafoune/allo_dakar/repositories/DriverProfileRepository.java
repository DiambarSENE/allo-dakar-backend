package sn.diafoune.allo_dakar.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.diafoune.allo_dakar.entities.DriverProfile;

import java.util.Optional;
import java.util.UUID;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, UUID> {

    Optional<DriverProfile> findByUserId(UUID userId);

    boolean existsByLicenseNumber(String licenseNumber);
}
