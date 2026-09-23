package sn.diafoune.allo_dakar.web.dtos.payment;

import jakarta.validation.constraints.NotNull;
import sn.diafoune.allo_dakar.entities.enums.PaymentMethod;

import java.util.UUID;

public record CreatePaymentRequest(
        @NotNull UUID bookingId,
        @NotNull PaymentMethod paymentMethod
) {
}
