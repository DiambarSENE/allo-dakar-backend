package sn.diafoune.allo_dakar.exceptions;

import java.time.Instant;
import java.util.List;

/**
 * Format standard d'erreur retourné par GlobalExceptionHandler. Ne jamais y inclure de
 * stack trace en production (voir application.yml server.error.*).
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        ErrorCode code,
        String message,
        String path,
        List<String> details
) {
}
