package sn.diafoune.allo_dakar.web.dtos.user;

import sn.diafoune.allo_dakar.entities.enums.UserStatus;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String profilePicture,
        UserStatus status,
        boolean emailVerified,
        boolean phoneVerified,
        Set<String> roles
) {
}
