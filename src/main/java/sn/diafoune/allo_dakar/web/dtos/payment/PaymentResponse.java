package sn.diafoune.allo_dakar.web.dtos.payment;

import sn.diafoune.allo_dakar.entities.enums.PaymentMethod;
import sn.diafoune.allo_dakar.entities.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        String paymentReference,
        UUID bookingId,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        Instant paidAt,
        String failureReason
) {
}
