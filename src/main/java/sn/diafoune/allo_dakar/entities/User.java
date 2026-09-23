package sn.diafoune.allo_dakar.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.diafoune.allo_dakar.entities.enums.UserStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Compte utilisateur. Les identifiants (mot de passe) sont entièrement gérés par Keycloak —
 * cette entité ne stocke plus aucun secret. keycloakId relie ce compte local au compte Keycloak
 * correspondant (nécessaire pour les opérations d'administration : désactivation, réinitialisation
 * de mot de passe, etc. via l'API Admin — voir security.keycloak.KeycloakAdminClient).
 *
 * L'identifiant interne ({@link BaseEntity#getId()}) reste la clé utilisée dans tout le reste de
 * l'application (relations JPA, SecurityUtils.currentUserId()) — il est distinct du "sub" du JWT
 * Keycloak (stocké dans keycloakId). Le compte local est créé automatiquement au premier appel
 * authentifié d'un nouveau "sub" (provisioning JIT, voir security.jwt.KeycloakUserProvisioningFilter)
 * — ce backend n'a plus d'endpoint d'inscription propre.
 *
 * IMPORTANT : ne jamais exposer cette entité directement dans un contrôleur — toujours passer
 * par un DTO (UserResponse, UserSummaryResponse, ...) via les mappers.
 */
@Getter
@Setter
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_phone", columnNames = "phone"),
                @UniqueConstraint(name = "uk_users_keycloak_id", columnNames = "keycloak_id")
        },
        indexes = {
                @Index(name = "idx_users_status", columnList = "status")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, length = 180)
    private String email;

    /**
     * Optionnel : Keycloak ne fournit pas systématiquement de numéro de téléphone à la création
     * du compte (aucun scope standard OIDC ne le porte par défaut) — complété ensuite via
     * PUT /api/v1/users/me si besoin. Voir KeycloakUserProvisioningFilter.
     */
    @Column(length = 20)
    private String phone;

    /** Identifiant du compte Keycloak correspondant (champ "id" de l'utilisateur côté Keycloak). */
    @Column(name = "keycloak_id", nullable = false, length = 64)
    private String keycloakId;

    private LocalDate dateOfBirth;

    private String profilePicture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean phoneVerified = false;

    private Instant lastLoginAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"})
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PassengerProfile passengerProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DriverProfile driverProfile;
}
