package sn.diafoune.allo_dakar.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Base de toutes les exceptions métier de l'API. Chaque sous-classe fixe son HttpStatus et un
 * ErrorCode stable (voir ErrorCode), utilisé par le frontend pour un traitement programmatique
 * indépendant du message (localisable) et du statut HTTP.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode code;
    private final List<String> details;

    protected ApiException(HttpStatus status, ErrorCode code, String message) {
        this(status, code, message, List.of());
    }

    protected ApiException(HttpStatus status, ErrorCode code, String message, List<String> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }
}
