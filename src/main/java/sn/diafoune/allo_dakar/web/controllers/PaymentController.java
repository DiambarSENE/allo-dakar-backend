package sn.diafoune.allo_dakar.web.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.diafoune.allo_dakar.commons.utils.PaginationUtils;
import sn.diafoune.allo_dakar.commons.utils.SecurityUtils;
import sn.diafoune.allo_dakar.services.interfaces.PaymentService;
import sn.diafoune.allo_dakar.web.dtos.common.ApiResponse;
import sn.diafoune.allo_dakar.web.dtos.common.PagedResponse;
import sn.diafoune.allo_dakar.web.dtos.payment.CreatePaymentRequest;
import sn.diafoune.allo_dakar.web.dtos.payment.PaymentResponse;

import java.util.UUID;

/**
 * initiate() délègue à un PaymentProvider (abstraction — voir services.payment). Aucun
 * fournisseur réel n'est intégré ; seul MockPaymentProvider est branché tant qu'aucune
 * spécification technique (Orange Money, Wave, Stripe...) n'est fournie — voir §16.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> initiate(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.initiate(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("Paiement initié", response));
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of("Paiement récupéré", paymentService.getById(id, SecurityUtils.currentUserId()));
    }

    @GetMapping("/me")
    public PagedResponse<PaymentResponse> myPayments(@RequestParam(required = false) Integer page,
                                                       @RequestParam(required = false) Integer size) {
        return PagedResponse.of("Paiements récupérés",
                paymentService.listMine(SecurityUtils.currentUserId(), PaginationUtils.of(page, size, null, null)));
    }
}
