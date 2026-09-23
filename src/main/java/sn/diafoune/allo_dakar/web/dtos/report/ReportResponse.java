package sn.diafoune.allo_dakar.web.dtos.report;

import sn.diafoune.allo_dakar.entities.enums.ReportReason;
import sn.diafoune.allo_dakar.entities.enums.ReportStatus;

import java.time.Instant;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        UUID reporterId,
        UUID reportedUserId,
        ReportReason reason,
        String description,
        ReportStatus status,
        String resolution,
        Instant resolvedAt
) {
}
