package sn.diafoune.allo_dakar.web.dtos.review;

import sn.diafoune.allo_dakar.entities.enums.ReviewStatus;

import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID tripId,
        UUID authorId,
        String authorFullName,
        UUID targetUserId,
        int rating,
        String comment,
        ReviewStatus status
) {
}
