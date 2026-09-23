package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.entities.AuditLog;
import sn.diafoune.allo_dakar.entities.DriverProfile;
import sn.diafoune.allo_dakar.entities.DriverVerification;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.DriverVerificationStatus;
import sn.diafoune.allo_dakar.entities.enums.VerificationWorkflowStatus;
import sn.diafoune.allo_dakar.exceptions.ResourceNotFoundException;
import sn.diafoune.allo_dakar.mappers.DriverMapper;
import sn.diafoune.allo_dakar.repositories.AuditLogRepository;
import sn.diafoune.allo_dakar.repositories.DriverProfileRepository;
import sn.diafoune.allo_dakar.repositories.DriverVerificationRepository;
import sn.diafoune.allo_dakar.repositories.UserRepository;
import sn.diafoune.allo_dakar.services.interfaces.DriverService;
import sn.diafoune.allo_dakar.services.interfaces.NotificationService;
import sn.diafoune.allo_dakar.services.interfaces.VerificationService;
import sn.diafoune.allo_dakar.entities.enums.NotificationType;
import sn.diafoune.allo_dakar.web.dtos.verification.ReviewVerificationRequest;
import sn.diafoune.allo_dakar.web.dtos.verification.SubmitVerificationRequest;
import sn.diafoune.allo_dakar.web.dtos.verification.VerificationResponse;

import java.time.Instant;
import java.util.UUID;

/**
 * Workflow §11 : l'administration consulte/approuve/rejette/demande des compléments. Une
 * approbation fait passer DriverProfile.verificationStatus à VERIFIED (règle §48-1 : condition
 * de publication d'un trajet).
 */
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final DriverVerificationRepository verificationRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;
    private final DriverService driverService;
    private final DriverMapper driverMapper;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public VerificationResponse submit(UUID driverUserId, SubmitVerificationRequest request) {
        DriverProfile driver = driverService.getOrCreateForUser(driverUserId);

        DriverVerification verification = DriverVerification.builder()
                .driver(driver)
                .documentType(request.documentType())
                .documentNumber(request.documentNumber())
                .documentUrl(request.documentUrl())
                .status(VerificationWorkflowStatus.SUBMITTED)
                .submittedAt(Instant.now())
                .build();

        driver.setVerificationStatus(DriverVerificationStatus.UNDER_REVIEW);
        driverProfileRepository.save(driver);

        return driverMapper.toResponse(verificationRepository.save(verification));
    }

    @Override
    public Page<VerificationResponse> listPending(Pageable pageable) {
        return verificationRepository.findByStatus(VerificationWorkflowStatus.SUBMITTED, pageable)
                .map(driverMapper::toResponse);
    }

    @Override
    @Transactional
    public VerificationResponse approve(UUID verificationId, UUID adminId, ReviewVerificationRequest request) {
        DriverVerification verification = getEntity(verificationId);
        verification.setStatus(VerificationWorkflowStatus.APPROVED);
        verification.setReviewedAt(Instant.now());
        verification.setReviewedBy(getAdmin(adminId));
        verification.setComment(request.comment());

        DriverProfile driver = verification.getDriver();
        driver.setVerificationStatus(DriverVerificationStatus.VERIFIED);
        driverProfileRepository.save(driver);
        verificationRepository.save(verification);

        audit(adminId, verification.getId(), "DRIVER_VERIFICATION_APPROVED");
        notificationService.notify(driver.getUser(), NotificationType.DRIVER_VERIFIED,
                "Vérification approuvée", "Votre profil conducteur a été vérifié, vous pouvez publier des trajets.");

        return driverMapper.toResponse(verification);
    }

    @Override
    @Transactional
    public VerificationResponse reject(UUID verificationId, UUID adminId, ReviewVerificationRequest request) {
        DriverVerification verification = getEntity(verificationId);
        verification.setStatus(VerificationWorkflowStatus.REJECTED);
        verification.setReviewedAt(Instant.now());
        verification.setReviewedBy(getAdmin(adminId));
        verification.setRejectionReason(request.rejectionReason());

        DriverProfile driver = verification.getDriver();
        driver.setVerificationStatus(DriverVerificationStatus.REJECTED);
        driverProfileRepository.save(driver);
        verificationRepository.save(verification);

        audit(adminId, verification.getId(), "DRIVER_VERIFICATION_REJECTED");
        notificationService.notify(driver.getUser(), NotificationType.DRIVER_VERIFICATION_REJECTED,
                "Vérification refusée", "Votre document de vérification a été refusé : " + request.rejectionReason());

        return driverMapper.toResponse(verification);
    }

    @Override
    @Transactional
    public VerificationResponse requireMoreInformation(UUID verificationId, UUID adminId, ReviewVerificationRequest request) {
        DriverVerification verification = getEntity(verificationId);
        verification.setStatus(VerificationWorkflowStatus.REQUIRES_MORE_INFORMATION);
        verification.setReviewedAt(Instant.now());
        verification.setReviewedBy(getAdmin(adminId));
        verification.setComment(request.comment());

        DriverProfile driver = verification.getDriver();
        driver.setVerificationStatus(DriverVerificationStatus.REQUIRES_MORE_INFORMATION);
        driverProfileRepository.save(driver);
        verificationRepository.save(verification);

        audit(adminId, verification.getId(), "DRIVER_VERIFICATION_MORE_INFO_REQUESTED");
        return driverMapper.toResponse(verification);
    }

    @Override
    public VerificationWorkflowStatus statusOf(UUID verificationId) {
        return getEntity(verificationId).getStatus();
    }

    private DriverVerification getEntity(UUID id) {
        return verificationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("DriverVerification", id));
    }

    private User getAdmin(UUID adminId) {
        return userRepository.findById(adminId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", adminId));
    }

    private void audit(UUID actorId, UUID entityId, String action) {
        auditLogRepository.save(AuditLog.builder()
                .actor(getAdmin(actorId))
                .entityType("DriverVerification")
                .entityId(entityId)
                .action(action)
                .build());
    }
}
