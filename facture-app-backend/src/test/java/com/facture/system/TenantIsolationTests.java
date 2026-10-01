package com.facture.system;

import com.facture.system.model.*;
import com.facture.system.repository.*;
import com.facture.system.security.SecurityConfig;
import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
@AutoConfigureMockMvc
@Transactional
class TenantIsolationTests {
    @Autowired MockMvc mvc;
    @Autowired AdminRepository admins;
    @Autowired ClientRepository clients;
    @Autowired InvoiceRepository invoices;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtEncoder encoder;
    @Autowired ObjectMapper json;
    private Admin alice;
    private Client aliceClient;
    private Client bobClient;
    private String token;

    @BeforeEach
    void setup() throws Exception {
        alice = admins.save(new Admin("alice@test.example", passwords.encode("Alice-test-2026!")));
        var bob = admins.save(new Admin("bob@test.example", passwords.encode("Bob-test-2026!")));
        aliceClient = clients.save(new Client("Alice client", "a@test.example", alice));
        bobClient = clients.save(new Client("Bob client", "b@test.example", bob));
        invoices.save(new Invoice(new BigDecimal("12.50"), LocalDate.now(), "Alice invoice", aliceClient));
        invoices.save(new Invoice(new BigDecimal("99.00"), LocalDate.now(), "Bob invoice", bobClient));
        var response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"alice@test.example","password":"Alice-test-2026!"}
                    """))
            .andExpect(status().isOk()).andExpect(jsonPath("$.expiresIn").value(3600))
            .andReturn().getResponse().getContentAsString();
        token = json.readTree(response).get("token").asText();
    }

    @Test
    void requiresAValidToken() throws Exception {
        mvc.perform(get("/api/clients")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/clients").header("Authorization", "Bearer invalid"))
            .andExpect(status().isUnauthorized());
        var expired = JwtClaimsSet.builder().issuer(SecurityConfig.ISSUER)
            .subject(alice.getId().toString()).issuedAt(Instant.now().minusSeconds(7200))
            .expiresAt(Instant.now().minusSeconds(3600)).build();
        String expiredToken = encoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(), expired)).getTokenValue();
        mvc.perform(get("/api/clients").header("Authorization", "Bearer " + expiredToken))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsBadPassword() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"alice@test.example","password":"wrong"}
                """)).andExpect(status().isUnauthorized());
    }

    @Test
    void listsOnlyOwnedDataWithoutExposingEntities() throws Exception {
        mvc.perform(get("/api/clients").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("Alice client"))
            .andExpect(jsonPath("$[0].admin").doesNotExist())
            .andExpect(jsonPath("$[0].password").doesNotExist());
        mvc.perform(get("/api/clients/{id}/invoices", aliceClient.getId())
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].description").value("Alice invoice"));
    }

    @Test
    void refusesCrossTenantReadsAndWrites() throws Exception {
        mvc.perform(get("/api/clients/{id}/invoices", bobClient.getId())
            .header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
        mvc.perform(get("/api/clients/{id}/invoices", Long.MAX_VALUE)
            .header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
        long before = invoices.count();
        mvc.perform(post("/api/clients/{id}/invoices", bobClient.getId())
            .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"amount":10.50,"date":"2026-10-01","description":"Forbidden"}
                """)).andExpect(status().isNotFound());
        assertEquals(before, invoices.count());
    }

    @Test
    void createsClientForAuthenticatedAdmin() throws Exception {
        var result = mvc.perform(post("/api/clients").header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"New client","email":"new@test.example"}
                """)).andExpect(status().isCreated()).andReturn();
        Long id = json.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        assertEquals(alice.getId(), clients.findById(id).orElseThrow().getAdmin().getId());
    }

    @Test
    void allowsOwnedInvoiceCreationAndValidatesAmounts() throws Exception {
        mvc.perform(post("/api/clients/{id}/invoices", aliceClient.getId())
            .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"amount":42.25,"date":"2026-10-01","description":"Allowed"}
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.amount").value(42.25));
        mvc.perform(post("/api/clients/{id}/invoices", aliceClient.getId())
            .header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"amount":-1,"date":"2026-10-01","description":"Invalid"}
                """)).andExpect(status().isBadRequest());
    }
}
