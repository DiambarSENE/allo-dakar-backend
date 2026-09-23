package sn.diafoune.allo_dakar.web.dtos.user;

import jakarta.validation.constraints.NotBlank;

public record SuspendUserRequest(
        @NotBlank String reason
) {
}
