package sn.diafoune.allo_dakar.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.diafoune.allo_dakar.entities.Role;
import sn.diafoune.allo_dakar.entities.User;
import sn.diafoune.allo_dakar.entities.enums.RoleType;
import sn.diafoune.allo_dakar.entities.enums.UserStatus;
import sn.diafoune.allo_dakar.repositories.RoleRepository;
import sn.diafoune.allo_dakar.repositories.UserRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provisioning "just-in-time" : en architecture "frontend direct", ce backend n'a jamais
 * l'occasion de créer le compte local au moment de l'inscription (qui se fait entièrement côté
 * Keycloak). Au premier appel authentifié d'un nouveau compte Keycloak, ce filtre crée
 * silencieusement la ligne User correspondante à partir des claims du token, puis rend son ID
 * interne disponible via un attribut de requête (lu par SecurityUtils.currentUserId()) — le
 * "sub" du JWT reste l'identifiant Keycloak, jamais l'ID interne (voir entities.User).
 *
 * Les rôles locaux (User.roles) sont resynchronisés depuis realm_access.roles à CHAQUE requête,
 * pas seulement à la création — sinon un rôle assigné/retiré dans la console Keycloak après le
 * premier login resterait indéfiniment invisible côté base (voir syncRolesIfNeeded). Cette table
 * reste un miroir d'affichage (UserResponse.roles) : les décisions d'autorisation elles-mêmes
 * (@PreAuthorize) lisent toujours les authorities du JWT en direct, jamais cette table.
 *
 * Vérifie aussi le statut local (SUSPENDED/BANNED) à chaque requête : un compte suspendu chez
 * nous doit être bloqué même si son token Keycloak est encore valide et que la synchronisation
 * enabled=false (KeycloakAdminClient.setEnabled, déclenchée à la suspension) n'a pas encore
 * révoqué sa session active côté IdP.
 */
@Component
@RequiredArgsConstructor
public class KeycloakUserProvisioningFilter extends OncePerRequestFilter {

    public static final String CURRENT_USER_ID_ATTRIBUTE = "sn.diafoune.allo_dakar.currentUserId";

    private static final Logger log = LoggerFactory.getLogger(KeycloakUserProvisioningFilter.class);

    private static final Set<String> KNOWN_ROLES = Arrays.stream(RoleType.values())
            .map(Enum::name)
            .collect(java.util.stream.Collectors.toSet());

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            User user = ensureUserExists(jwtAuth.getToken());

            if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.BANNED) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"status\":403,\"error\":\"FORBIDDEN\",\"code\":\"ACCOUNT_SUSPENDED\",\"message\":\"Ce compte est suspendu ou banni\"}");
                return;
            }

            request.setAttribute(CURRENT_USER_ID_ATTRIBUTE, user.getId());
        }

        chain.doFilter(request, response);
    }

    //@Transactional
    protected User ensureUserExists(Jwt jwt) {
        String keycloakId = jwt.getSubject();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> provision(jwt, keycloakId));
        syncRolesIfNeeded(user, jwt);
        return user;
    }

    private User provision(Jwt jwt, String keycloakId) {
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            // Cas limite : un client mal configuré n'a pas demandé le scope "email". On retombe
            // sur une valeur dérivée du sub pour respecter la contrainte NOT NULL/UNIQUE plutôt
            // que d'échouer la requête — l'utilisateur devra compléter son profil ensuite.
            email = keycloakId + "@unresolved.allodakar.sn";
            log.warn("Token Keycloak sans claim 'email' (sub={}) — email de secours généré", keycloakId);
        }
        String firstName = firstNonBlank(jwt.getClaimAsString("given_name"), jwt.getClaimAsString("preferred_username"), "Utilisateur");
        String lastName = firstNonBlank(jwt.getClaimAsString("family_name"), "Allo Dakar");

        User user = User.builder()
                .keycloakId(keycloakId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .status(UserStatus.ACTIVE)
                .roles(resolveRoles(jwt))
                .build();

        user = userRepository.save(user);
        log.info("USER_PROVISIONED_FROM_KEYCLOAK userId={} keycloakId={}", user.getId(), keycloakId);
        return user;
    }

    /**
     * Resynchronise les rôles locaux à CHAQUE requête authentifiée (pas seulement à la création
     * du compte) — sinon un changement de rôle fait dans la console Keycloak après le premier
     * login de l'utilisateur ne serait jamais répercuté côté base (bug rapporté en pratique :
     * rôle assigné dans Keycloak mais absent de UserResponse.roles). Le coût est négligeable
     * (comparaison d'ensembles en mémoire, écriture seulement si différence réelle).
     */
    private void syncRolesIfNeeded(User user, Jwt jwt) {
        Set<Role> tokenRoles = resolveRoles(jwt);
        if (!tokenRoles.equals(user.getRoles())) {
            Set<Role> previous = user.getRoles();
            user.setRoles(tokenRoles);
            userRepository.save(user);
            log.info("USER_ROLES_RESYNCED_FROM_KEYCLOAK userId={} previousRoles={} newRoles={}",
                    user.getId(), roleNames(previous), roleNames(tokenRoles));
        }
    }

    private static Set<String> roleNames(Set<Role> roles) {
        return roles.stream().map(r -> r.getName().name()).collect(java.util.stream.Collectors.toSet());
    }

    /** Synchronise les rôles locaux à partir de realm_access.roles — PASSENGER par défaut si aucun rôle connu. */
    private Set<Role> resolveRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        Set<String> tokenRoles = new HashSet<>();
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
            roles.stream().map(Object::toString).filter(KNOWN_ROLES::contains).forEach(tokenRoles::add);
        }
        if (tokenRoles.isEmpty()) {
            tokenRoles.add(RoleType.PASSENGER.name());
        }

        Set<Role> resolved = new HashSet<>();
        for (String roleName : tokenRoles) {
            roleRepository.findByName(RoleType.valueOf(roleName)).ifPresent(resolved::add);
        }
        return resolved;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
