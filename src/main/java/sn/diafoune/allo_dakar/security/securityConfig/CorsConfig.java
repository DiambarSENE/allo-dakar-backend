package sn.diafoune.allo_dakar.security.securityConfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Expose UNIQUEMENT un CorsConfigurationSource — ne PAS enregistrer de CorsFilter séparément
 * via FilterRegistrationBean : un tel filtre s'enregistre dans la chaîne de filtres Servlet
 * générique, dont l'ordre par défaut (0) est APRÈS celui de la chaîne Spring Security
 * (-100 par défaut). Un preflight OPTIONS sur un endpoint protégé se ferait alors rejeter en
 * 401 par Spring Security avant même d'atteindre le filtre CORS — le navigateur ne verrait
 * jamais les en-têtes CORS et rapporterait un échec CORS (bug rencontré en pratique). La bonne
 * intégration est de passer ce bean à HttpSecurity.cors(...) dans SecurityConfig, qui l'insère
 * au bon endroit dans SA PROPRE chaîne, avant l'autorisation.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
