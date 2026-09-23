package sn.diafoune.allo_dakar.security.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * serverUrl doit être l'URL telle qu'accessible par CE service (ex. http://keycloak:8080 en
 * Docker Compose, http://localhost:8081 en local) — voir docker-compose.yml.
 *
 * adminClientId/adminClientSecret correspondent au client confidentiel "allo-dakar-backend-admin"
 * (service account, rôle "manage-users" sur le client realm-management — voir
 * keycloak/realm-export.json), utilisé UNIQUEMENT pour l'API Admin (ex. désactiver un compte
 * suspendu). Ce backend n'authentifie plus aucun utilisateur final : c'est un pur Resource
 * Server, le client web/mobile s'authentifie directement auprès de Keycloak (Authorization Code
 * + PKCE) — voir README §13.
 */
@ConfigurationProperties(prefix = "app.keycloak")
public record KeycloakProperties(
        String serverUrl,
        String realm,
        String adminClientId,
        String adminClientSecret
) {
}
