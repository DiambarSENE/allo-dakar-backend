-- Keycloak (architecture "frontend direct") ne fournit pas systématiquement de numéro de
-- téléphone à la création du compte : aucun scope OIDC standard ne le porte par défaut. Le
-- provisioning JIT (KeycloakUserProvisioningFilter) ne peut donc plus garantir cette valeur.
ALTER TABLE users ALTER COLUMN phone DROP NOT NULL;
