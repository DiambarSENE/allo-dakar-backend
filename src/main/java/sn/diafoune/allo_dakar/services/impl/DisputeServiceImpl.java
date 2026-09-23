package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.entities.AuditLog;
import sn.diafoune.allo_dakar.entities.Booking;
import sn.diafoune.allo_dakar.entities.Dispute;
import sn.diafoune.allo_dakar.entities.Payment;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.DisputeStatus;
import sn.diafoune.allo_dakar.exceptions.BadRequestException;
import sn.diafoune.allo_dakar.exceptions.ResourceNotFoundException;
import sn.diafoune.allo_dakar.mappers.DisputeMapper;
import sn.diafoune.allo_dakar.repositories.AuditLogRepository;
import sn.diafoune.allo_dakar.repositories.BookingRepository;
import sn.diafoune.allo_dakar.repositories.DisputeRepository;
import sn.diafoune.allo_dakar.repositories.PaymentRepository;
import sn.diafoune.allo_dakar.repositories.UserRepository;
import sn.diafoune.allo_dakar.services.interfaces.DisputeService;
import sn.diafoune.allo_dakar.web.dtos.dispute.CreateDisputeRequest;
import sn.diafoune.allo_dakar.web.dtos.dispute.DisputeResponse;

import java.time.Instant;
import java.util.UUID;

/**
 * §21/§51 du cahier des charges backend, complété pour le besoin frontend §43 (service client) :
 * aucun endpoint de création n'existait jusqu'ici pour Dispute — sans lui, "consulter les
 * litiges" côté service client n'aurait jamais rien eu à afficher. Un passager ou un conducteur
 * peut lever un litige sur SA PROPRE réservation/paiement ; la résolution reste une action
 * ADMIN (voir DisputeService.resolve).
 */
@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final DisputeMapper disputeMapper;
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public DisputeResponse create(UUID raisedByUserId, CreateDisputeRequest request) {
        User raisedBy = userRepository.findById(raisedByUserId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", raisedByUserId));

        Booking booking = null;
        if (request.bookingId() != null) {
            booking = bookingRepository.findById(request.bookingId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Booking", request.bookingId()));
            boolean isParticipant = booking.getPassenger().getId().equals(raisedByUserId)
                    || booking.getTrip().getDriver().getUser().getId().equals(raisedByUserId);
            if (!isParticipant) {
                throw new BadRequestException("Vous ne pouvez ouvrir un litige que sur votre propre réservation");
            }
        }

        Payment payment = null;
        if (request.paymentId() != null) {
            payment = paymentRepository.findById(request.paymentId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Payment", request.paymentId()));
            if (!payment.getUser().getId().equals(raisedByUserId)) {
                throw new BadRequestException("Vous ne pouvez ouvrir un litige que sur votre propre paiement");
            }
        }

        Dispute dispute = Dispute.builder()
                .raisedBy(raisedBy)
                .booking(booking)
                .payment(payment)
                .type(request.type())
                .status(DisputeStatus.OPEN)
                .description(request.description())
                .build();

        return disputeMapper.toResponse(disputeRepository.save(dispute));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisputeResponse> listMine(UUID userId, Pageable pageable) {
        return disputeRepository.findByRaisedById(userId, pageable).map(disputeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisputeResponse> listAll(Pageable pageable) {
        return disputeRepository.findAll(pageable).map(disputeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DisputeResponse getById(UUID id) {
        return disputeMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional
    public DisputeResponse resolve(UUID id, UUID resolvedByAdminId, String resolution, boolean resolved) {
        Dispute dispute = getEntity(id);
        User admin = userRepository.findById(resolvedByAdminId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", resolvedByAdminId));

        dispute.setStatus(resolved ? DisputeStatus.RESOLVED : DisputeStatus.REJECTED);
        dispute.setResolution(resolution);
        dispute.setResolvedAt(Instant.now());
        dispute = disputeRepository.save(dispute);

        auditLogRepository.save(AuditLog.builder()
                .actor(admin)
                .entityType("Dispute")
                .entityId(dispute.getId())
                .action(resolved ? "DISPUTE_RESOLVED" : "DISPUTE_REJECTED")
                .details(resolution)
                .build());

        return disputeMapper.toResponse(dispute);
    }

    private Dispute getEntity(UUID id) {
        return disputeRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Dispute", id));
    }
}
