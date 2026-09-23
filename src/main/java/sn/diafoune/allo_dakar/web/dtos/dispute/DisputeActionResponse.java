package sn.diafoune.allo_dakar.web.dtos.dispute;

import java.time.Instant;
import java.util.UUID;

public record DisputeActionResponse(
        UUID id,
        String performedByFullName,
        String action,
        String note,
        Instant createdAt
) {
}
