package sn.diafoune.allo_dakar.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.diafoune.allo_dakar.entities.DriverVerification;
import sn.diafoune.allo_dakar.entities.enums.VerificationWorkflowStatus;

import java.util.List;
import java.util.UUID;

public interface DriverVerificationRepository extends JpaRepository<DriverVerification, UUID> {

    List<DriverVerification> findByDriverId(UUID driverId);

    Page<DriverVerification> findByStatus(VerificationWorkflowStatus status, Pageable pageable);
}
