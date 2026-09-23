package sn.diafoune.allo_dakar.exceptions;

import org.springframework.http.HttpStatus;

public class BookingException extends ApiException {

    public BookingException(ErrorCode code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
}
