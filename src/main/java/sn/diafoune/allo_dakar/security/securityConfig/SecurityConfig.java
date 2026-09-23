package sn.diafoune.allo_dakar.security.securityConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfigurationSource;
import sn.diafoune.allo_dakar.security.jwt.CustomJwtAuthenticationConverter;
import sn.diafoune.allo_dakar.security.jwt.KeycloakUserProvisioningFilter;

/**
 * API stateless, pur Resource Server OAuth2 — Keycloak est l'Authorization Server. Ce backend
 * n'expose plus AUCUN endpoint d'authentification (/register, /login, /refresh, /logout) : le
 * client web/mobile s'authentifie directement auprès de Keycloak (Authorization Code + PKCE) et
 * présente son access token ici. CSRF désactivé (non pertinent sans session cookie). Tous les
 * endpoints sont protégés par défaut ; seuls ceux listés dans PUBLIC_GET_ENDPOINTS sont ouverts —
 * voir §38.
 *
 * Le JwtDecoder est auto-configuré par Spring Boot à partir de
 * spring.security.oauth2.resourceserver.jwt.issuer-uri (voir application.yml), qui va chercher
 * la JWK Set de Keycloak. KeycloakUserProvisioningFilter tourne juste après l'authentification
 * JWT pour résoudre/créer le compte local correspondant (voir cette classe pour le détail).
 *
 * CORS : la configuration (origines autorisées) vit dans CorsConfig, mais c'est ICI, via
 * .cors(...), qu'elle doit être branchée — un CorsFilter enregistré séparément passerait après
 * la chaîne Spring Security et ne protégerait pas les preflights des endpoints authentifiés.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/v1/trips/search",
            "/api/v1/trips/*",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomJwtAuthenticationConverter converter,
                                                     KeycloakUserProvisioningFilter provisioningFilter,
                                                     CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Un preflight CORS (OPTIONS) ne porte jamais de header Authorization : le
                        // laisser passer ici est nécessaire pour que le navigateur voie les en-têtes
                        // CORS ajoutés par .cors(...) ci-dessus, avant toute vérification d'auth réelle
                        // sur la requête effective qui suit (bug CORS rencontré en pratique, voir
                        // CorsConfig pour le détail de la cause).
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN")
                        .requestMatchers("/api/v1/customer-service/**").hasAnyRole("ADMIN", "CUSTOMER_SERVICE")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .addFilterAfter(provisioningFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Renvoie une 401 JSON cohérente (via GlobalExceptionHandler) plutôt que la page par défaut
     * de Spring Security lorsqu'aucun token n'est fourni.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":401,\"error\":\"UNAUTHORIZED\",\"code\":\"UNAUTHORIZED\",\"message\":\"Authentification requise\"}");
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(403);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":403,\"error\":\"FORBIDDEN\",\"code\":\"FORBIDDEN\",\"message\":\"Accès refusé\"}");
        };
    }
}
