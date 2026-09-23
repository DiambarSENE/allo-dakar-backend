package sn.diafoune.allo_dakar.exceptions;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Erreur de validation métier explicite (au-delà de la validation Bean Validation déjà gérée
 * par MethodArgumentNotValidException dans GlobalExceptionHandler).
 */
public class ValidationException extends ApiException {

    public ValidationException(String message, List<String> details) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, message, details);
    }
}
