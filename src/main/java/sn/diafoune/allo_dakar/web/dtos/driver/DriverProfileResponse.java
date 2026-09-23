package sn.diafoune.allo_dakar.web.dtos.driver;

import sn.diafoune.allo_dakar.entities.enums.DriverVerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DriverProfileResponse(
        UUID id,
        UUID userId,
        String licenseNumber,
        LocalDate licenseExpiryDate,
        DriverVerificationStatus verificationStatus,
        BigDecimal averageRating,
        int totalTrips,
        int totalReviews
) {
}
