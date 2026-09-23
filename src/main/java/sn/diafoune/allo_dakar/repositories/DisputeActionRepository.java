package sn.diafoune.allo_dakar.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.diafoune.allo_dakar.entities.DisputeAction;

import java.util.List;
import java.util.UUID;

public interface DisputeActionRepository extends JpaRepository<DisputeAction, UUID> {

    List<DisputeAction> findByDisputeIdOrderByCreatedAtAsc(UUID disputeId);
}
