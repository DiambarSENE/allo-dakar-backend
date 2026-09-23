package sn.diafoune.allo_dakar.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.diafoune.allo_dakar.commons.utils.PaginationUtils;
import sn.diafoune.allo_dakar.commons.utils.SecurityUtils;
import sn.diafoune.allo_dakar.services.interfaces.DisputeService;
import sn.diafoune.allo_dakar.web.dtos.common.ApiResponse;
import sn.diafoune.allo_dakar.web.dtos.common.PagedResponse;
import sn.diafoune.allo_dakar.web.dtos.dispute.CreateDisputeRequest;
import sn.diafoune.allo_dakar.web.dtos.dispute.DisputeResponse;

/**
 * Un litige (§21/§51) peut être levé par un passager ou un conducteur sur SA PROPRE réservation
 * ou paiement (vérifié dans DisputeServiceImpl). La consultation globale et la résolution sont
 * réservées au service client/admin — voir CustomerServiceController et AdminController.
 */
@RestController
@RequestMapping("/api/v1/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    public ResponseEntity<ApiResponse<DisputeResponse>> create(@Valid @RequestBody CreateDisputeRequest request) {
        DisputeResponse response = disputeService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Litige créé", response));
    }

    @GetMapping("/me")
    public PagedResponse<DisputeResponse> myDisputes(@RequestParam(required = false) Integer page,
                                                        @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Litiges récupérés",
                disputeService.listMine(SecurityUtils.currentUserId(), PaginationUtils.of(page, size, null, null)));
    }
}
