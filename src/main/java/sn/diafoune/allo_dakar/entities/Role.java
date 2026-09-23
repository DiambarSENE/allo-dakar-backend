package sn.diafoune.allo_dakar.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.diafoune.allo_dakar.entities.enums.RoleType;

/**
 * Rôle applicatif. Modélisé comme entité (plutôt qu'un simple enum sur User) pour rester
 * extensible sans migration de code — voir Phase 1, ambiguïté #1.
 */
@Getter
@Setter
@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 40)
    private RoleType name;

    @Column(length = 255)
    private String description;
}
