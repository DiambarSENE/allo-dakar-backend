package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.commons.utils.ReferenceGenerator;
import sn.diafoune.allo_dakar.entities.Booking;
import sn.diafoune.allo_dakar.entities.Payment;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.BookingStatus;
import sn.diafoune.allo_dakar.entities.enums.NotificationType;
import sn.diafoune.allo_dakar.entities.enums.PaymentStatus;
import sn.diafoune.allo_dakar.exceptions.BusinessException;
import sn.diafoune.allo_dakar.exceptions.ErrorCode;
import sn.diafoune.allo_dakar.exceptions.ForbiddenException;
import sn.diafoune.allo_dakar.exceptions.ResourceNotFoundException;
import sn.diafoune.allo_dakar.mappers.PaymentMapper;
import sn.diafoune.allo_dakar.repositories.BookingRepository;
import sn.diafoune.allo_dakar.repositories.PaymentRepository;
import sn.diafoune.allo_dakar.services.interfaces.BookingService;
import sn.diafoune.allo_dakar.services.interfaces.NotificationService;
import sn.diafoune.allo_dakar.services.interfaces.PaymentService;
import sn.diafoune.allo_dakar.services.interfaces.UserService;
import sn.diafoune.allo_dakar.services.payment.PaymentProvider;
import sn.diafoune.allo_dakar.web.dtos.payment.CreatePaymentRequest;
import sn.diafoune.allo_dakar.web.dtos.payment.PaymentResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Règle §48-3 : Booking.status ne passe à CONFIRMED que si un Payment atteint SUCCESS — c'est ici
 * que la bascule se fait, en appelant BookingService.confirmAfterPayment.
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final BookingService bookingService;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;
    private final List<PaymentProvider> providers;

    @Override
    @Transactional
    public PaymentResponse initiate(UUID userId, CreatePaymentRequest request) {
        User user = userService.getEntityById(userId);
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> ResourceNotFoundException.of("Booking", request.bookingId()));

        if (!booking.getPassenger().getId().equals(userId)) {
            throw new ForbiddenException("Cette réservation n'appartient pas à cet utilisateur");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_AVAILABLE,
                    "Cette réservation n'est plus en attente de paiement (statut : " + booking.getStatus() + ")");
        }

        Payment payment = Payment.builder()
                .paymentReference(ReferenceGenerator.paymentReference())
                .booking(booking)
                .user(user)
                .amount(booking.getTotalAmount())
                .paymentMethod(request.paymentMethod())
                .status(PaymentStatus.PROCESSING)
                .build();

        PaymentProvider provider = providers.stream()
                .filter(p -> p.supports(request.paymentMethod()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_FAILED_ERROR,
                        "Aucun fournisseur de paiement disponible pour " + request.paymentMethod()));

        provider.execute(payment);
        payment = paymentRepository.save(payment);

        booking.setPaymentStatus(payment.getStatus());
        bookingRepository.save(booking);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.info("PAYMENT_SUCCESS paymentId={} bookingId={}", payment.getId(), booking.getId());
            bookingService.confirmAfterPayment(booking.getId());
        } else if (payment.getStatus() == PaymentStatus.FAILED) {
            payment.setFailedAt(Instant.now());
            paymentRepository.save(payment);
            log.warn("PAYMENT_FAILED paymentId={} bookingId={}", payment.getId(), booking.getId());
            notificationService.notify(user, NotificationType.PAYMENT_FAILED,
                    "Paiement échoué", "Le paiement de votre réservation " + booking.getBookingReference() + " a échoué.");
        }

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse getById(UUID paymentId, UUID requestingUserId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment", paymentId));
        if (!payment.getUser().getId().equals(requestingUserId)) {
            throw new ForbiddenException("Ce paiement n'appartient pas à cet utilisateur");
        }
        return paymentMapper.toResponse(payment);
    }

    @Override
    public Page<PaymentResponse> listMine(UUID userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable).map(paymentMapper::toResponse);
    }
}
