package sn.diafoune.allo_dakar.security.keycloak;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

/**
 * Intégration minimale avec l'API Admin de Keycloak (RestClient — pas de SDK admin-client, pour
 * éviter une dépendance de plus dont les versions doivent suivre Keycloak de près).
 *
 * En architecture "frontend direct" (voir Phase 1 / échange sur la sécurité), ce backend
 * n'authentifie plus jamais un utilisateur final : Keycloak gère entièrement inscription,
 * connexion, refresh et mot de passe oublié via son propre flux (Authorization Code + PKCE, côté
 * client web/mobile). Le SEUL besoin résiduel côté backend est de pouvoir désactiver un compte
 * Keycloak quand un ADMIN suspend l'utilisateur correspondant côté métier (règle §48-7) — sans
 * cela, un utilisateur suspendu dans notre base pourrait quand même obtenir un nouveau token
 * auprès de Keycloak, puisque l'authentification ne passe plus par nous.
 *
 * Le token admin est redemandé à chaque appel plutôt que mis en cache — plus simple et largement
 * suffisant au faible volume d'appels d'administration.
 */
@Component
@RequiredArgsConstructor
public class KeycloakAdminClient {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminClient.class);

    private final KeycloakProperties properties;
    private final RestClient restClient = RestClient.create();

    /** Active/désactive le compte Keycloak — bloque immédiatement toute nouvelle authentification. */
    public void setEnabled(String keycloakUserId, boolean enabled) {
        if (keycloakUserId == null) {
            log.warn("Impossible de synchroniser enabled={} avec Keycloak : keycloakUserId absent", enabled);
            return;
        }
        String adminToken = fetchAdminToken();
        try {
            restClient.put()
                    .uri("{server}/admin/realms/{realm}/users/{id}", properties.serverUrl(), properties.realm(), keycloakUserId)
                    .headers(h -> h.setBearerAuth(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("enabled", enabled))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.error("Échec de mise à jour du statut enabled={} pour le compte Keycloak {} : {}",
                    enabled, keycloakUserId, ex.getMessage());
            throw new KeycloakIntegrationException("Impossible de mettre à jour le statut du compte Keycloak");
        }
    }

    private String fetchAdminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", properties.adminClientId());
        form.add("client_secret", properties.adminClientSecret());

        try {
            KeycloakTokenResponse response = restClient.post()
                    .uri("{server}/realms/{realm}/protocol/openid-connect/token", properties.serverUrl(), properties.realm())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KeycloakTokenResponse.class);
            if (response == null) {
                throw new KeycloakIntegrationException("Réponse vide de Keycloak lors de l'authentification admin");
            }
            return response.accessToken();
        } catch (RestClientResponseException ex) {
            log.error("Échec d'authentification du client admin Keycloak : {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new KeycloakIntegrationException("Impossible de s'authentifier auprès de Keycloak (client admin)");
        }
    }
}
