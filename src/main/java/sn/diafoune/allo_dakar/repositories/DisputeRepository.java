package sn.diafoune.allo_dakar.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.diafoune.allo_dakar.entities.Dispute;
import sn.diafoune.allo_dakar.entities.enums.DisputeStatus;

import java.util.UUID;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {

    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);

    Page<Dispute> findByRaisedById(UUID userId, Pageable pageable);
}
