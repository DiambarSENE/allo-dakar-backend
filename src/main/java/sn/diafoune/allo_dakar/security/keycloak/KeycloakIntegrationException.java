package sn.diafoune.allo_dakar.security.keycloak;

import org.springframework.http.HttpStatus;
import sn.diafoune.allo_dakar.exceptions.ApiException;
import sn.diafoune.allo_dakar.exceptions.ErrorCode;

/**
 * Échec d'un appel à Keycloak (Admin API ou endpoint token) qui n'est pas un problème
 * d'identifiants utilisateur (voir BadCredentialsException pour ce cas) — ex. Keycloak
 * injoignable, configuration invalide, conflit de création de compte.
 */
public class KeycloakIntegrationException extends ApiException {

    public KeycloakIntegrationException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, message);
    }
}
