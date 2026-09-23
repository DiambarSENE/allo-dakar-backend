package sn.diafoune.allo_dakar.exceptions;

import org.springframework.http.HttpStatus;

public class PaymentException extends ApiException {

    public PaymentException(ErrorCode code, String message) {
        super(HttpStatus.PAYMENT_REQUIRED, code, message);
    }
}
