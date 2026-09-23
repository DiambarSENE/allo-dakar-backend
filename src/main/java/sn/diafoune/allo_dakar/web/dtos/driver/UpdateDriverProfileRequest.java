package sn.diafoune.allo_dakar.web.dtos.driver;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateDriverProfileRequest(
        @Size(max = 60) String licenseNumber,
        @Future LocalDate licenseExpiryDate
) {
}
