package sn.diafoune.allo_dakar.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Violation d'une règle métier générique (ex. conducteur non vérifié, trajet non publiable,
 * avis en double). Statut HTTP 422 par défaut ; utiliser ConflictException/BookingException/
 * PaymentException pour les cas nécessitant un autre statut.
 */
public class BusinessException extends ApiException {

    public BusinessException(ErrorCode code, String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }
}
