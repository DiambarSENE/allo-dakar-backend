package sn.diafoune.allo_dakar.services.payment;

import sn.diafoune.allo_dakar.entities.Payment;
import sn.diafoune.allo_dakar.entities.enums.PaymentMethod;

/**
 * Abstraction d'exécution de paiement — voir §16. Aucune intégration réelle (Orange Money, Wave,
 * Stripe...) n'est branchée sans spécification technique fournie par le client ; seul un
 * MockPaymentProvider est actif tant qu'aucun fournisseur n'est précisé.
 */
public interface PaymentProvider {

    boolean supports(PaymentMethod method);

    /**
     * Exécute le paiement et met à jour payment.status/transactionId/paidAt/failedAt en place.
     * Ne lève pas d'exception métier pour un échec de paiement "normal" (FAILED est un résultat
     * valide) — seules les erreurs techniques inattendues sont propagées.
     */
    void execute(Payment payment);
}
