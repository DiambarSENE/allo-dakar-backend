package sn.diafoune.allo_dakar.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Journal d'audit générique (référence libre entityType + entityId) plutôt qu'une FK stricte
 * vers chaque entité métier, pour rester extensible sans migration à chaque nouveau cas d'usage.
 * Ne jamais y écrire de mot de passe, JWT complet, ou donnée bancaire — voir §46.
 */
@Getter
@Setter
@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_logs_entity", columnList = "entity_type, entity_id"),
                @Index(name = "idx_audit_logs_actor", columnList = "actor_id")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "entity_type", nullable = false, length = 60)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(nullable = false, length = 60)
    private String action;

    /** Détails structurés (JSON sérialisé en texte) — jamais de secret/donnée sensible brute. */
    @Column(columnDefinition = "text")
    private String details;
}
