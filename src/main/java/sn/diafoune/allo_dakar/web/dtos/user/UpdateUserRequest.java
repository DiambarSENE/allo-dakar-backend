package sn.diafoune.allo_dakar.web.dtos.user;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** PATCH partiel : tous les champs sont optionnels, seuls les non-null sont appliqués. */
public record UpdateUserRequest(
        @Size(max = 80) String firstName,
        @Size(max = 80) String lastName,
        @Size(max = 20) String phone,
        @Past LocalDate dateOfBirth,
        String profilePicture
) {
}
