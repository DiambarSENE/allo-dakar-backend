-- Passage à Keycloak comme fournisseur d'identité : les identifiants (mot de passe) ne sont
-- plus stockés localement, ils sont entièrement gérés par Keycloak. keycloak_id relie chaque
-- compte local à son compte Keycloak (utilisé par l'API Admin pour désactiver un compte,
-- déclencher un email de réinitialisation, etc.).
ALTER TABLE users DROP COLUMN password;
ALTER TABLE users ADD COLUMN keycloak_id VARCHAR(64);

-- NOT NULL appliqué séparément : le projet n'étant pas encore en production, aucune donnée
-- existante à migrer. Si des utilisateurs existaient déjà, il faudrait d'abord les créer côté
-- Keycloak et renseigner keycloak_id avant cette contrainte.
ALTER TABLE users ALTER COLUMN keycloak_id SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT uk_users_keycloak_id UNIQUE (keycloak_id);

DROP TABLE IF EXISTS revoked_tokens;
