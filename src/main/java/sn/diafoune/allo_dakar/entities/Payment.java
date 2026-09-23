package sn.diafoune.allo_dakar.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.diafoune.allo_dakar.entities.enums.PaymentMethod;
import sn.diafoune.allo_dakar.entities.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Une Booking peut avoir plusieurs Payment (tentatives successives) ; au plus un SUCCESS est
 * retenu comme faisant foi. L'intégration réelle d'un fournisseur (Orange Money, Wave, Stripe...)
 * n'est PAS implémentée ici : voir services.interfaces.PaymentProvider pour l'abstraction prévue.
 */
@Getter
@Setter
@Entity
@Table(
        name = "payments",
        uniqueConstraints = @UniqueConstraint(name = "uk_payments_reference", columnNames = "payment_reference")
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Payment extends BaseEntity {

    @Column(name = "payment_reference", nullable = false, length = 30)
    private String paymentReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "XOF";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /** Identifiant renvoyé par le provider externe (mock tant qu'aucun fournisseur réel n'est branché). */
    private String transactionId;

    private Instant paidAt;
    private Instant failedAt;

    @Column(length = 500)
    private String failureReason;
}
