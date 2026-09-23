# Allo Dakar — Backend

API REST de la plateforme de covoiturage **Allo Dakar**, pour le contexte sénégalais.

> Le système de jetons/crédits et le système d'abonnements du cahier des charges initial ont été
> **volontairement exclus** de ce projet, à la demande du client.

## 1. Présentation

Allo Dakar met en relation conducteurs et passagers pour du covoiturage : recherche et publication
de trajets, réservation de places, paiement, avis, vérification des conducteurs, signalements et
administration de la plateforme.

## 2. Architecture

Architecture en couches, package racine `sn.diafoune.allo_dakar` :

```
commons/        utilitaires génériques (DateUtils, SecurityUtils, ReferenceGenerator,
                PaginationUtils) et configuration (Security, OpenAPI, CORS, Jackson, audit JPA)
exceptions/     hiérarchie ApiException + GlobalExceptionHandler (@RestControllerAdvice)
entities/       entités JPA + enums métier (entities/enums)
mappers/        conversion Entity <-> DTO (jamais dans les contrôleurs)
repositories/   Spring Data JPA + spécifications de recherche (TripSpecifications)
security/       validation JWT (issuer Keycloak), provisioning JIT, config Spring Security
                security/keycloak/ : client Admin API Keycloak (KeycloakAdminClient)
services/       interfaces + implémentations (logique métier), providers de paiement
web/
  controllers/  contrôleurs REST (fins : validation -> service -> réponse HTTP)
  dtos/         objets de transfert, un sous-package par domaine
```

Un `Trip`, `Booking`, `Payment` etc. ne sont **jamais** exposés directement : tout passe par un DTO
et un mapper dédié.

## 3. Technologies

- Java 25, Spring Boot 4.0.x (Spring Framework 7)
- Spring Web, Spring Data JPA, Spring Security, OAuth2 Resource Server
- **Keycloak 26** — Authorization Server (OIDC). Ce backend est un pur Resource Server : il ne
  gère plus ni inscription, ni connexion, ni mot de passe — voir §13
- PostgreSQL 17, Flyway
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, AssertJ, Testcontainers
- Docker / Docker Compose

## 4. Prérequis

- JDK 25
- Maven 3.9+
- Docker et Docker Compose (pour PostgreSQL local et/ou les tests d'intégration Testcontainers)

## 5. Installation

```bash
git clone <repo>
cd allo-dakar-backend
cp .env.example .env   # puis éditer .env avec de vraies valeurs locales
```

## 6. Configuration

La configuration suit les profils Spring standards :

- `application.yml` — configuration commune
- `application-dev.yml` — développement local
- `application-test.yml` — exécution des tests (Testcontainers)
- `application-prod.yml` — production (toutes les valeurs viennent de variables d'environnement)

## 7. Variables d'environnement

Voir `.env.example`. Principales :

| Variable | Description |
|---|---|
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Connexion PostgreSQL (application) |
| `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD` | Compte admin de la console Keycloak elle-même |
| `KEYCLOAK_ISSUER_URI` | Issuer vu par le **backend** pour valider les tokens (JWK Set) |
| `KEYCLOAK_SERVER_URL` / `KEYCLOAK_REALM` | Base URL + realm, utilisés par `KeycloakAdminClient` |
| `KEYCLOAK_ADMIN_CLIENT_ID` / `_SECRET` | Client confidentiel `allo-dakar-backend-admin` (service account, rôle `manage-users`) — doit correspondre à `keycloak/realm-export.json` |
| `CORS_ALLOWED_ORIGINS` | Origines autorisées (séparées par des virgules) |

⚠️ **Piège classique en Docker Compose** : l'`issuer` comparé littéralement dans chaque JWT doit
correspondre exactement au host utilisé par le **client** (navigateur/mobile) pour obtenir ses
tokens — pas forcément le même host que celui utilisé par le backend en interne. Voir le
commentaire dans `docker-compose.yml` et `KeycloakProperties`.

Aucun secret réel n'est committé : `.env` et `application-local.yml` sont dans `.gitignore`.

## 8. Base de données

PostgreSQL 17. Le schéma est entièrement piloté par les migrations Flyway — aucune génération
automatique du schéma par Hibernate (`ddl-auto: validate`).

## 9. Migrations Flyway

Les migrations sont dans `src/main/resources/db/migration` (`V1__...` à `V13__...`), appliquées
automatiquement au démarrage. Pour les rejouer manuellement :

```bash
mvn flyway:migrate
```

## 10. Lancement local

```bash
# Démarrer uniquement PostgreSQL
docker compose up -d db

# Lancer l'application
mvn spring-boot:run
```

L'API est alors disponible sur `http://localhost:8080/api/v1`.

## 11. Docker

```bash
docker compose up --build
```

Démarre PostgreSQL (application), PostgreSQL (Keycloak), Keycloak 26 (realm importé
automatiquement depuis `keycloak/realm-export.json`) et le backend — voir §13 pour la
configuration Keycloak au préalable. Nécessite un accès à Maven Central pour la construction de
l'image (étape `mvn package` dans le `Dockerfile`).

## 12. Swagger

Documentation interactive : `http://localhost:8080/swagger-ui.html`
Spécification OpenAPI brute : `http://localhost:8080/v3/api-docs`

## 13. Authentification

**Keycloak est l'Authorization Server ; ce backend est un pur Resource Server.** Il n'expose plus
aucun endpoint `/register`, `/login`, `/refresh` ou `/logout` — le client web/mobile s'authentifie
**directement auprès de Keycloak** via Authorization Code + PKCE, puis présente son access token à
l'API dans le header `Authorization: Bearer <access_token>`.

Ce que fait (et ne fait pas) le backend :

- **Valide** les tokens via Spring Security OAuth2 Resource Server, configuré avec l'`issuer-uri`
  de Keycloak (`spring.security.oauth2.resourceserver.jwt.issuer-uri`) — JWK Set récupérée
  automatiquement, aucun secret partagé à gérer côté backend pour la validation.
- **Extrait les rôles** du claim `realm_access.roles` du token (`CustomJwtAuthenticationConverter`)
  pour alimenter `@PreAuthorize("hasRole('...')")`.
- **Provisionne "just-in-time"** le compte local (`User`) au premier appel authentifié d'un
  nouveau `sub` Keycloak (`KeycloakUserProvisioningFilter`), à partir des claims du token
  (email, given_name, family_name) — ce backend n'a plus jamais l'occasion de créer un compte au
  moment de l'inscription, qui se fait entièrement côté Keycloak.
- **Synchronise la suspension** : quand un ADMIN suspend un utilisateur (`POST
  /api/v1/admin/users/{id}/suspend`), `KeycloakAdminClient` désactive aussi son compte Keycloak
  (`enabled: false`) — sinon un compte suspendu localement pourrait quand même obtenir un nouveau
  token, l'authentification ne passant plus par ce backend.
- Ne stocke **aucun mot de passe** : `User` n'a plus de champ `password`, uniquement `keycloakId`
  (le `sub` du compte Keycloak correspondant).

### Setup Keycloak (local / Docker Compose)

Le realm, les rôles et les deux clients (`allo-dakar-frontend` public, `allo-dakar-backend-admin`
confidentiel avec service account) sont pré-configurés par `keycloak/realm-export.json`, importé
automatiquement au démarrage (`--import-realm`). Avant `docker compose up` :

1. Renseigner `KEYCLOAK_ADMIN_CLIENT_SECRET` dans `.env` avec la **même valeur** que le champ
   `"secret"` du client `allo-dakar-backend-admin` dans `keycloak/realm-export.json` (changez les
   deux si vous ne gardez pas la valeur d'exemple).
2. `docker compose up -d keycloak` puis, une fois démarré, `http://localhost:8081` (console
   admin, identifiants `KEYCLOAK_ADMIN` / `KEYCLOAK_ADMIN_PASSWORD`) pour vérifier l'import et
   ajuster `redirectUris`/`webOrigins` du client `allo-dakar-frontend` selon l'URL réelle de votre
   frontend.
3. Un compte de démonstration `admin.demo@allodakar.sn` (rôle `ADMIN`, mot de passe temporaire
   `ChangeMe123!`) est créé par l'import — à supprimer ou changer en dehors d'un usage local.

Le client web/mobile doit implémenter le flux Authorization Code + PKCE contre
`http://localhost:8081/realms/allo-dakar` (bibliothèques usuelles : `keycloak-js`,
`react-oidc-context`, `AppAuth` pour mobile...) — hors périmètre de ce backend.

## 14. Tests

```bash
mvn test
```

- Tests unitaires (Mockito) : règles métier de `ReviewServiceImpl`, `TripServiceImpl` (vérification
  conducteur), `BookingServiceImpl` (disponibilité des places), `PaymentServiceImpl` (succès/échec)
- Tests d'intégration (Testcontainers + PostgreSQL réel) :
  - `BookingConcurrencyIntegrationTest` — deux réservations concurrentes ne dépassent jamais la
    capacité d'un trajet (voir §15/§16 ci-dessous)
  - `SecurityAccessIntegrationTest` — accès public / authentifié / par rôle

Les tests d'intégration nécessitent Docker (Testcontainers démarre un vrai PostgreSQL).

## 15. Structure du projet

```
allo-dakar-backend/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── keycloak/realm-export.json
├── src/main/java/sn/diafoune/allo_dakar/...
├── src/main/resources/{application*.yml, db/migration/}
└── src/test/java/sn/diafoune/allo_dakar/{services,integration}/
```

## 16. Principales règles métier

1. Un conducteur ne peut publier que si `User.status = ACTIVE` et (selon
   `app.business.driver-verification-required-to-publish`) `DriverProfile.verificationStatus = VERIFIED`.
2. Une réservation ne peut jamais dépasser `Trip.availableSeats`.
3. **Concurrence** : verrouillage optimiste (`@Version` sur `Trip`) + retry applicatif borné (3
   tentatives, transaction `REQUIRES_NEW` isolée dans `SeatReservationService`) plutôt que
   `PESSIMISTIC_WRITE`, pour ne pas bloquer les lectures concurrentes sur un trajet à fort trafic.
   Une contrainte `CHECK (available_seats >= 0)` en base sert de filet de sécurité indépendant.
4. Une réservation ne passe `CONFIRMED` que si un `Payment` associé atteint `SUCCESS`.
5. Un avis n'est possible qu'après `Trip.status = COMPLETED`, et seulement par un participant réel.
6. Une réservation annulée libère les places uniquement si elles avaient été décomptées
   (`PENDING`/`CONFIRMED`, jamais `REJECTED`).
7. Toutes les opérations financières et administratives sensibles sont journalisées (`AuditLog`).
8. Un trajet expiré (date de départ passée) n'est plus réservable — basculé par un job planifié
   (`@Scheduled`, toutes les 15 minutes).
9. Le rôle `CUSTOMER_SERVICE` a un périmètre volontairement restreint et séparé d'`ADMIN` —
   voir `CustomerServiceController` (`/api/v1/customer-service/**`) : consultation des
   utilisateurs/réservations/trajets/litiges, et annulation de réservation pour le compte d'un
   utilisateur (`BookingService.cancelOnBehalf`, motif obligatoire, journalisé). La résolution
   des litiges et la suspension de comptes restent strictement `ADMIN`.

## 17. Déploiement

Build de l'artefact :

```bash
mvn clean package
java -jar target/allo-dakar-backend.jar --spring.profiles.active=prod
```

Toutes les valeurs sensibles (base de données, secret JWT, origines CORS) doivent venir de
variables d'environnement en production — jamais de valeurs par défaut committées.

## 18. Sécurité

- Mots de passe : entièrement gérés par Keycloak, jamais stockés ni transités par ce backend
- Validation JWT déléguée à Spring Security OAuth2 Resource Server (JWK Set de Keycloak,
  auto-découverte via `issuer-uri`) — aucun secret de signature géré côté backend
- Documents de vérification conducteur : jamais exposés dans un DTO public
- CORS restreint aux origines configurées ; CSRF désactivé (API stateless)
- Endpoints protégés par défaut ; liste explicite des endpoints publics dans `SecurityConfig`
- Le client `allo-dakar-backend-admin` (service account) ne détient que le rôle client
  `manage-users` sur `realm-management` — pas de droits d'administration plus larges
- Un compte suspendu localement est aussi désactivé côté Keycloak (`KeycloakAdminClient`), pour
  qu'il ne puisse plus obtenir de nouveau token
- Limitation connue : le rôle `MODERATOR` n'a pas encore de route dédiée distincte d'`ADMIN`
  (actuellement `/api/v1/admin/**` est restreint à `ADMIN` seul)

## 19. Roadmap

- Intégration réelle d'un fournisseur de paiement (Orange Money, Wave, Stripe...) derrière
  l'abstraction `PaymentProvider` déjà en place (actuellement `MockPaymentProvider` uniquement)
- Envoi effectif des notifications EMAIL/SMS/PUSH (actuellement IN_APP uniquement)
- Route dédiée pour le rôle `MODERATOR`
- Système de jetons/crédits et système d'abonnements (hors périmètre de cette version — voir
  note en tête de ce document)
