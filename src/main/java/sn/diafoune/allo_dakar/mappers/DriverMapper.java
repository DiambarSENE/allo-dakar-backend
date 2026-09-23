package sn.diafoune.allo_dakar.mappers;

import org.springframework.stereotype.Component;
import sn.diafoune.allo_dakar.entities.DriverProfile;
import sn.diafoune.allo_dakar.entities.DriverVerification;
import sn.diafoune.allo_dakar.web.dtos.driver.DriverProfileResponse;
import sn.diafoune.allo_dakar.web.dtos.verification.VerificationResponse;

@Component
public class DriverMapper {

    public DriverProfileResponse toResponse(DriverProfile profile) {
        if (profile == null) {
            return null;
        }
        return new DriverProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getLicenseNumber(),
                profile.getLicenseExpiryDate(),
                profile.getVerificationStatus(),
                profile.getAverageRating(),
                profile.getTotalTrips(),
                profile.getTotalReviews()
        );
    }

    /** Ne mappe jamais documentUrl — voir DriverVerification et règle §54 (données sensibles). */
    public VerificationResponse toResponse(DriverVerification verification) {
        if (verification == null) {
            return null;
        }
        return new VerificationResponse(
                verification.getId(),
                verification.getDriver().getId(),
                verification.getDocumentType(),
                verification.getStatus(),
                verification.getSubmittedAt(),
                verification.getReviewedAt(),
                verification.getRejectionReason()
        );
    }
}
