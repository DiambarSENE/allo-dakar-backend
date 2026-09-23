package sn.diafoune.allo_dakar.entities.enums;

/**
 * Statut global de vérification du profil conducteur (agrégat des DriverVerification individuelles).
 */
public enum DriverVerificationStatus {
    PENDING,
    UNDER_REVIEW,
    VERIFIED,
    REJECTED,
    REQUIRES_MORE_INFORMATION
}
