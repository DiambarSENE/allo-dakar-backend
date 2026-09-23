//package sn.diafoune.allo_dakar.integration;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import sn.diafoune.allo_dakar.entities.enums.RoleType;
//
//import java.util.UUID;
//
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
///**
// * Couvre §44 "Sécurité" : endpoint public, endpoint authentifié, endpoint réservé à un rôle
// * (PASSENGER/DRIVER/ADMIN). Keycloak étant l'Authorization Server (voir README §13), ce test
// * n'appelle jamais un vrai Keycloak : il injecte directement un JwtAuthenticationToken via
// * SecurityMockMvcRequestPostProcessors.jwt(...), avec les autorités "ROLE_x" explicites — ce qui
// * isole ce test à la fois des règles métier d'authentification ET de CustomJwtAuthenticationConverter
// * (déjà couvert isolément si besoin). Le premier appel authentifié déclenche aussi le
// * provisioning JIT local (KeycloakUserProvisioningFilter), exercé implicitement par ces tests.
// */
//@Testcontainers
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//class SecurityAccessIntegrationTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
//            .withDatabaseName("allo_dakar_test")
//            .withUsername("allo_dakar_test")
//            .withPassword("allo_dakar_test");
//
//    @DynamicPropertySource
//    static void datasourceProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//    }
//
//    @Autowired private MockMvc mockMvc;
//
//    /** Simule un token Keycloak valide pour un rôle donné, sans appel réseau réel à Keycloak. */
//    private JwtRequestPostProcessor tokenFor(RoleType roleType) {
//        String sub = UUID.randomUUID().toString();
//        return jwt().jwt(builder -> builder
//                        .subject(sub)
//                        .claim("email", roleType.name().toLowerCase() + "-" + sub + "@allodakar.sn")
//                        .claim("given_name", "Test")
//                        .claim("family_name", roleType.name())
//                        .claim("realm_access", java.util.Map.of("roles", java.util.List.of(roleType.name()))))
//                .authorities(new SimpleGrantedAuthority("ROLE_" + roleType.name()));
//    }
//
//    @Test
//    void rechercheDeTrajetsEstPublique() throws Exception {
//        mockMvc.perform(get("/api/v1/trips/search"))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void profilUtilisateurRequiertAuthentification() throws Exception {
//        mockMvc.perform(get("/api/v1/users/me"))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    void profilUtilisateurAccessibleAvecTokenValide() throws Exception {
//        mockMvc.perform(get("/api/v1/users/me").with(tokenFor(RoleType.PASSENGER)))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void endpointPassagerRefuseUnConducteur() throws Exception {
//        mockMvc.perform(get("/api/v1/bookings/me").with(tokenFor(RoleType.DRIVER)))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void endpointPassagerAccessibleAUnPassager() throws Exception {
//        mockMvc.perform(get("/api/v1/bookings/me").with(tokenFor(RoleType.PASSENGER)))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    void endpointAdminRefuseUnPassager() throws Exception {
//        mockMvc.perform(get("/api/v1/admin/users").with(tokenFor(RoleType.PASSENGER)))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void endpointAdminAccessibleAUnAdmin() throws Exception {
//        mockMvc.perform(get("/api/v1/admin/users").with(tokenFor(RoleType.ADMIN)))
//                .andExpect(status().isOk());
//    }
//}
