package sn.diafoune.allo_dakar.web.dtos.dispute;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sn.diafoune.allo_dakar.entities.enums.DisputeType;

import java.util.UUID;

/** bookingId et paymentId sont tous deux optionnels, mais au moins un contexte (booking OU
 * payment) est recommandé pour instruire le litige — vérifié en service plutôt qu'en validation
 * stricte, pour rester extensible (type BEHAVIOR peut ne référencer ni l'un ni l'autre). */
public record CreateDisputeRequest(
        @NotNull DisputeType type,
        UUID bookingId,
        UUID paymentId,
        @Size(max = 1000) String description
) {
}
