package sn.diafoune.allo_dakar.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import sn.diafoune.allo_dakar.entities.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    /** Utilisé pour retrouver le compte local à partir du "sub" d'un token Keycloak si besoin. */
    java.util.Optional<User> findByKeycloakId(String keycloakId);

    /** §43 "Rechercher un utilisateur" (service client) — recherche insensible à la casse sur email/prénom/nom. */
    @org.springframework.data.jpa.repository.Query(
            "SELECT u FROM User u WHERE lower(u.email) LIKE lower(concat('%', :search, '%')) "
                    + "OR lower(u.firstName) LIKE lower(concat('%', :search, '%')) "
                    + "OR lower(u.lastName) LIKE lower(concat('%', :search, '%'))")
    org.springframework.data.domain.Page<User> search(String search, org.springframework.data.domain.Pageable pageable);
}
