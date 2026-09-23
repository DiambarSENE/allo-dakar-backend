package sn.diafoune.allo_dakar.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.diafoune.allo_dakar.web.dtos.payment.CreatePaymentRequest;
import sn.diafoune.allo_dakar.web.dtos.payment.PaymentResponse;

import java.util.UUID;

public interface PaymentService {

    /**
     * Délègue l'exécution effective à un PaymentProvider (abstraction — voir services.payment).
     * Aucun fournisseur réel (Orange Money, Wave, Stripe...) n'est intégré ; seul un provider
     * MOCK est branché tant qu'aucune spécification technique n'est fournie (voir §16).
     */
    PaymentResponse initiate(UUID userId, CreatePaymentRequest request);

    PaymentResponse getById(UUID paymentId, UUID requestingUserId);

    Page<PaymentResponse> listMine(UUID userId, Pageable pageable);
}
