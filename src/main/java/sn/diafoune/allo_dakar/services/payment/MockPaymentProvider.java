package sn.diafoune.allo_dakar.services.payment;

import org.springframework.stereotype.Component;
import sn.diafoune.allo_dakar.entities.Payment;
import sn.diafoune.allo_dakar.entities.enums.PaymentMethod;
import sn.diafoune.allo_dakar.entities.enums.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Provider de démonstration : toute méthode de paiement non explicitement intégrée passe par
 * ici et est marquée SUCCESS immédiatement (mock). À remplacer par de vrais providers
 * (MobileMoneyPaymentProvider, CardPaymentProvider...) dès qu'une spécification technique et des
 * identifiants fournisseur réels sont disponibles — voir §16, ne jamais prétendre le contraire.
 */
@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public boolean supports(PaymentMethod method) {
        return true;
    }

    @Override
    public void execute(Payment payment) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("MOCK-" + UUID.randomUUID());
        payment.setPaidAt(Instant.now());
    }
}
