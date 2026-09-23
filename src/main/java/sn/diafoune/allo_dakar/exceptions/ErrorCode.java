package sn.diafoune.allo_dakar.exceptions;

/**
 * Codes d'erreur métier stables, utilisés dans ApiErrorResponse.code — destinés à être
 * consommés par le frontend indépendamment du message (localisable) et du statut HTTP.
 */
public enum ErrorCode {
    RESOURCE_NOT_FOUND,
    BAD_REQUEST,
    VALIDATION_FAILED,
    UNAUTHORIZED,
    FORBIDDEN,
    CONFLICT,
    BOOKING_NOT_AVAILABLE,
    BOOKING_SEATS_UNAVAILABLE,
    TRIP_NOT_PUBLISHABLE,
    TRIP_NOT_AVAILABLE,
    DRIVER_NOT_VERIFIED,
    PAYMENT_FAILED_ERROR,
    DUPLICATE_REVIEW,
    REVIEW_NOT_ALLOWED,
    ACCOUNT_SUSPENDED,
    INVALID_CREDENTIALS,
    INTERNAL_ERROR
}
