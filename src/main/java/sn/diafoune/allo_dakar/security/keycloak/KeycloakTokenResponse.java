package sn.diafoune.allo_dakar.security.keycloak;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Réponse brute du endpoint /realms/{realm}/protocol/openid-connect/token de Keycloak. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") long expiresInSeconds,
        @JsonProperty("token_type") String tokenType
) {
}
