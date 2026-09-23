package sn.diafoune.allo_dakar.web.dtos.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateReviewRequest(
        @Min(1) @Max(5) Integer rating,
        @Size(max = 1000) String comment
) {
}
