package sn.diafoune.allo_dakar.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.diafoune.allo_dakar.web.dtos.dispute.CreateDisputeRequest;
import sn.diafoune.allo_dakar.web.dtos.dispute.DisputeResponse;

import java.util.UUID;

public interface DisputeService {

    DisputeResponse create(UUID raisedByUserId, CreateDisputeRequest request);

    Page<DisputeResponse> listMine(UUID userId, Pageable pageable);

    /** Réservé à ADMIN/CUSTOMER_SERVICE — voir SecurityConfig, préfixe /api/v1/customer-service. */
    Page<DisputeResponse> listAll(Pageable pageable);

    DisputeResponse getById(UUID id);

    /** Réservé à ADMIN — la résolution reste une décision administrative, pas une simple consultation. */
    DisputeResponse resolve(UUID id, UUID resolvedByAdminId, String resolution, boolean resolved);
}
