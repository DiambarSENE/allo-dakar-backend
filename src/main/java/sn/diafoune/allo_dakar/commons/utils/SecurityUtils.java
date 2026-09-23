package sn.diafoune.allo_dakar.commons.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sn.diafoune.allo_dakar.exceptions.UnauthorizedException;
import sn.diafoune.allo_dakar.security.jwt.KeycloakUserProvisioningFilter;

import java.util.UUID;

/**
 * Le "sub" d'un JWT Keycloak est l'ID du compte Keycloak — PAS l'ID interne de User (voir
 * entities.User). L'ID interne est résolu par KeycloakUserProvisioningFilter (provisioning JIT à
 * partir du "sub", pas d'aller-retour Keycloak nécessaire) et déposé comme attribut de requête ;
 * on le lit ici plutôt que de refaire une requête base à chaque appel.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID currentUserId() {
        // S'assure qu'un token Keycloak valide est bien présent avant de chercher l'attribut,
        // pour renvoyer une 401 explicite plutôt qu'une NPE si le filtre n'a pas tourné.
        currentJwtAuth();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        Object userId = request.getAttribute(KeycloakUserProvisioningFilter.CURRENT_USER_ID_ATTRIBUTE);
        if (userId == null) {
            throw new UnauthorizedException("Compte non synchronisé (provisioning JIT non exécuté)");
        }
        return (UUID) userId;
    }

    /** ID du compte Keycloak (le "sub" du token) — utile pour les appels à KeycloakAdminClient. */
    public static String currentKeycloakId() {
        return currentJwtAuth().getToken().getSubject();
    }

    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private static JwtAuthenticationToken currentJwtAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new UnauthorizedException("Aucun utilisateur authentifié");
        }
        return jwtAuth;
    }
}
