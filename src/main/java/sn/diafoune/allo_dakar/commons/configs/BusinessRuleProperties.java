package sn.diafoune.allo_dakar.commons.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Règles métier configurables sans redéploiement de code — voir §48 Règle 4 : la contrainte
 * de vérification conducteur doit rester centralisée et facilement modifiable.
 */
@ConfigurationProperties(prefix = "app.business")
public record BusinessRuleProperties(
        boolean driverVerificationRequiredToPublish
) {
}
