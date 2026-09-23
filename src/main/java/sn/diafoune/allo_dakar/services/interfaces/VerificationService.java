package sn.diafoune.allo_dakar.services.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.diafoune.allo_dakar.entities.enums.VerificationWorkflowStatus;
import sn.diafoune.allo_dakar.web.dtos.verification.ReviewVerificationRequest;
import sn.diafoune.allo_dakar.web.dtos.verification.SubmitVerificationRequest;
import sn.diafoune.allo_dakar.web.dtos.verification.VerificationResponse;

import java.util.UUID;

public interface VerificationService {

    VerificationResponse submit(UUID driverUserId, SubmitVerificationRequest request);

    Page<VerificationResponse> listPending(Pageable pageable);

    VerificationResponse approve(UUID verificationId, UUID adminId, ReviewVerificationRequest request);

    VerificationResponse reject(UUID verificationId, UUID adminId, ReviewVerificationRequest request);

    VerificationResponse requireMoreInformation(UUID verificationId, UUID adminId, ReviewVerificationRequest request);

    VerificationWorkflowStatus statusOf(UUID verificationId);
}
