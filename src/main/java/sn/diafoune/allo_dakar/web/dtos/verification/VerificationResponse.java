package sn.diafoune.allo_dakar.web.dtos.verification;

import sn.diafoune.allo_dakar.entities.enums.DocumentType;
import sn.diafoune.allo_dakar.entities.enums.VerificationWorkflowStatus;

import java.time.Instant;
import java.util.UUID;

/** documentUrl est volontairement absent — jamais exposé dans un DTO grand public. */
public record VerificationResponse(
        UUID id,
        UUID driverId,
        DocumentType documentType,
        VerificationWorkflowStatus status,
        Instant submittedAt,
        Instant reviewedAt,
        String rejectionReason
) {
}
