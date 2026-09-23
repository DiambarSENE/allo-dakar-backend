package sn.diafoune.allo_dakar.web.dtos.dispute;

import sn.diafoune.allo_dakar.entities.enums.DisputeStatus;
import sn.diafoune.allo_dakar.entities.enums.DisputeType;

import java.time.Instant;
import java.util.UUID;

public record DisputeResponse(
        UUID id,
        UUID raisedByUserId,
        String raisedByFullName,
        UUID bookingId,
        UUID paymentId,
        DisputeType type,
        DisputeStatus status,
        String description,
        String resolution,
        Instant resolvedAt,
        Instant createdAt
) {
}
