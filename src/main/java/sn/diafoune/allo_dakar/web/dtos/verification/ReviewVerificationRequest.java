package sn.diafoune.allo_dakar.web.dtos.verification;

public record ReviewVerificationRequest(
        String comment,
        String rejectionReason
) {
}
