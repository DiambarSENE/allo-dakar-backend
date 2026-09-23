package sn.diafoune.allo_dakar.web.dtos.verification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import sn.diafoune.allo_dakar.entities.enums.DocumentType;

public record SubmitVerificationRequest(
        @NotNull DocumentType documentType,
        String documentNumber,
        @NotBlank String documentUrl
) {
}
