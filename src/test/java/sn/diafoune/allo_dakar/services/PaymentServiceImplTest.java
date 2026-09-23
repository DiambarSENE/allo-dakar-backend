package sn.diafoune.allo_dakar.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.diafoune.allo_dakar.entities.Booking;
import sn.diafoune.allo_dakar.entities.Payment;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.BookingStatus;
import sn.diafoune.allo_dakar.entities.enums.PaymentMethod;
import sn.diafoune.allo_dakar.entities.enums.PaymentStatus;
import sn.diafoune.allo_dakar.mappers.PaymentMapper;
import sn.diafoune.allo_dakar.repositories.BookingRepository;
import sn.diafoune.allo_dakar.repositories.PaymentRepository;
import sn.diafoune.allo_dakar.services.impl.PaymentServiceImpl;
import sn.diafoune.allo_dakar.services.interfaces.BookingService;
import sn.diafoune.allo_dakar.services.interfaces.NotificationService;
import sn.diafoune.allo_dakar.services.interfaces.UserService;
import sn.diafoune.allo_dakar.services.payment.PaymentProvider;
import sn.diafoune.allo_dakar.web.dtos.payment.CreatePaymentRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Couvre §44 "Paiement" : SUCCESS -> Booking CONFIRMED (via confirmAfterPayment), FAILED ->
 * réservation non confirmée.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserService userService;
    @Mock private BookingService bookingService;
    @Mock private PaymentMapper paymentMapper;
    @Mock private NotificationService notificationService;

    private UUID userId;
    private Booking booking;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder().build();
        user.setId(userId);

        booking = Booking.builder()
                .passenger(user)
                .status(BookingStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(5000))
                .build();
        booking.setId(UUID.randomUUID());

        lenient().when(userService.getEntityById(userId)).thenReturn(user);
        lenient().when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        lenient().when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void paiementReussiConfirmeLaReservation() {
        PaymentProvider successProvider = new PaymentProvider() {
            @Override
            public boolean supports(PaymentMethod method) {
                return true;
            }

            @Override
            public void execute(Payment payment) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaidAt(Instant.now());
            }
        };
        PaymentServiceImpl service = new PaymentServiceImpl(paymentRepository, bookingRepository,
                userService, bookingService, paymentMapper, notificationService, List.of(successProvider));

        CreatePaymentRequest request = new CreatePaymentRequest(booking.getId(), PaymentMethod.MOBILE_MONEY);
        service.initiate(userId, request);

        verify(bookingService).confirmAfterPayment(booking.getId());
        assertThat(booking.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void paiementEchoueNeConfirmePasLaReservation() {
        PaymentProvider failingProvider = new PaymentProvider() {
            @Override
            public boolean supports(PaymentMethod method) {
                return true;
            }

            @Override
            public void execute(Payment payment) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Solde insuffisant");
            }
        };
        PaymentServiceImpl service = new PaymentServiceImpl(paymentRepository, bookingRepository,
                userService, bookingService, paymentMapper, notificationService, List.of(failingProvider));

        CreatePaymentRequest request = new CreatePaymentRequest(booking.getId(), PaymentMethod.MOBILE_MONEY);
        service.initiate(userId, request);

        verify(bookingService, never()).confirmAfterPayment(any());
        assertThat(booking.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
    }
}
