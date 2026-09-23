package sn.diafoune.allo_dakar.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.diafoune.allo_dakar.entities.enums.DocumentType;
import sn.diafoune.allo_dakar.entities.enums.VerificationWorkflowStatus;

import java.time.Instant;

/**
 * Soumission d'un document de vérification par un conducteur. documentUrl ne doit jamais être
 * exposé dans un DTO public — accessible uniquement via des endpoints ADMIN/MODERATOR dédiés.
 */
@Getter
@Setter
@Entity
@Table(name = "driver_verifications")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DriverVerification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverProfile driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentType documentType;

    @Column(length = 80)
    private String documentNumber;

    /** Référence/chemin de stockage du document — jamais retourné tel quel dans un DTO public. */
    @Column(nullable = false)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private VerificationWorkflowStatus status = VerificationWorkflowStatus.SUBMITTED;

    @Column(nullable = false)
    private Instant submittedAt;

    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(length = 500)
    private String rejectionReason;

    @Column(length = 500)
    private String comment;
}
