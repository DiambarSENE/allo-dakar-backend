package sn.diafoune.allo_dakar.mappers;

import org.springframework.stereotype.Component;
import sn.diafoune.allo_dakar.entities.Payment;
import sn.diafoune.allo_dakar.web.dtos.payment.PaymentResponse;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentReference(),
                payment.getBooking().getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getPaidAt(),
                payment.getFailureReason()
        );
    }
}
