package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.entities.AuditLog;
import sn.diafoune.allo_dakar.entities.Booking;
import sn.diafoune.allo_dakar.entities.Report;
import sn.diafoune.allo_dakar.entities.Review;
import sn.diafoune.allo_dakar.entities.Trip;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.NotificationType;
import sn.diafoune.allo_dakar.entities.enums.ReportStatus;
import sn.diafoune.allo_dakar.exceptions.BadRequestException;
import sn.diafoune.allo_dakar.exceptions.ResourceNotFoundException;
import sn.diafoune.allo_dakar.mappers.ReportMapper;
import sn.diafoune.allo_dakar.repositories.AuditLogRepository;
import sn.diafoune.allo_dakar.repositories.BookingRepository;
import sn.diafoune.allo_dakar.repositories.ReportRepository;
import sn.diafoune.allo_dakar.repositories.ReviewRepository;
import sn.diafoune.allo_dakar.repositories.TripRepository;
import sn.diafoune.allo_dakar.repositories.UserRepository;
import sn.diafoune.allo_dakar.services.interfaces.NotificationService;
import sn.diafoune.allo_dakar.services.interfaces.ReportService;
import sn.diafoune.allo_dakar.web.dtos.report.CreateReportRequest;
import sn.diafoune.allo_dakar.web.dtos.report.ReportResponse;

import java.time.Instant;
import java.util.UUID;

/**
 * Signalement d'un utilisateur, trajet, réservation ou avis (§20). Au moins une cible doit être
 * fournie. Le traitement (résolution/rejet) est réservé à l'administration/modération.
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final ReportMapper reportMapper;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ReportResponse create(UUID reporterId, CreateReportRequest request) {
        if (request.reportedUserId() == null && request.tripId() == null
                && request.bookingId() == null && request.reviewId() == null) {
            throw new BadRequestException("Un signalement doit cibler au moins un utilisateur, trajet, réservation ou avis");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", reporterId));

        User reportedUser = request.reportedUserId() != null
                ? userRepository.findById(request.reportedUserId())
                        .orElseThrow(() -> ResourceNotFoundException.of("User", request.reportedUserId()))
                : null;
        Trip trip = request.tripId() != null
                ? tripRepository.findById(request.tripId())
                        .orElseThrow(() -> ResourceNotFoundException.of("Trip", request.tripId()))
                : null;
        Booking booking = request.bookingId() != null
                ? bookingRepository.findById(request.bookingId())
                        .orElseThrow(() -> ResourceNotFoundException.of("Booking", request.bookingId()))
                : null;
        Review review = request.reviewId() != null
                ? reviewRepository.findById(request.reviewId())
                        .orElseThrow(() -> ResourceNotFoundException.of("Review", request.reviewId()))
                : null;

        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .trip(trip)
                .booking(booking)
                .review(review)
                .reason(request.reason())
                .description(request.description())
                .status(ReportStatus.OPEN)
                .build();

        return reportMapper.toResponse(reportRepository.save(report));
    }

    @Override
    public Page<ReportResponse> listMine(UUID reporterId, Pageable pageable) {
        return reportRepository.findByReporterId(reporterId, pageable).map(reportMapper::toResponse);
    }

    @Override
    public Page<ReportResponse> listAll(Pageable pageable) {
        return reportRepository.findAll(pageable).map(reportMapper::toResponse);
    }

    @Override
    @Transactional
    public ReportResponse handle(UUID reportId, UUID handledByAdminId, String resolution, boolean resolved) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> ResourceNotFoundException.of("Report", reportId));
        User admin = userRepository.findById(handledByAdminId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", handledByAdminId));

        report.setStatus(resolved ? ReportStatus.RESOLVED : ReportStatus.REJECTED);
        report.setHandledBy(admin);
        report.setResolution(resolution);
        report.setResolvedAt(Instant.now());
        reportRepository.save(report);

        auditLogRepository.save(AuditLog.builder()
                .actor(admin)
                .entityType("Report")
                .entityId(report.getId())
                .action(resolved ? "REPORT_RESOLVED" : "REPORT_REJECTED")
                .details(resolution)
                .build());

        notificationService.notify(report.getReporter(), NotificationType.REPORT_HANDLED,
                "Votre signalement a été traité",
                resolved ? "Votre signalement a été résolu." : "Votre signalement a été rejeté.");

        return reportMapper.toResponse(report);
    }
}
