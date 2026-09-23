package sn.diafoune.allo_dakar.web.dtos.report;

import jakarta.validation.constraints.NotNull;
import sn.diafoune.allo_dakar.entities.enums.ReportReason;

import java.util.UUID;

public record CreateReportRequest(
        UUID reportedUserId,
        UUID tripId,
        UUID bookingId,
        UUID reviewId,
        @NotNull ReportReason reason,
        String description
) {
}
