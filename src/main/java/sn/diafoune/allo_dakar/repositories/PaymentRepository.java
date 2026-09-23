package sn.diafoune.allo_dakar.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.diafoune.allo_dakar.entities.Payment;
import sn.diafoune.allo_dakar.entities.enums.PaymentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Page<Payment> findByUserId(UUID userId, Pageable pageable);

    List<Payment> findByBookingId(UUID bookingId);

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findFirstByBookingIdAndStatus(UUID bookingId, PaymentStatus status);
}
