package com.gateway.integration;

import com.gateway.security.GatewayJwtValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("Test wyłączony z powodu problemów z konfiguracją środowiska (niekompatybilne wersje bibliotek)")
class SecurityIntegrationTest {

    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext context;

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        public GatewayJwtValidator jwtValidator() {
            return Mockito.mock(GatewayJwtValidator.class);
        }
    }

    @Autowired
    private GatewayJwtValidator jwtValidator;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void shouldReturn401WhenNoTokenProvided() {
        webTestClient.get().uri("/api/v1/customers/profile")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn401ForInvalidToken() {
        when(jwtValidator.validate(anyString())).thenThrow(new JwtException("Invalid"));

        webTestClient.get().uri("/api/v1/customers/profile")
                .header("Authorization", "Bearer invalid")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn403ForInsufficientRole() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("role", "CUSTOMER");
        Claims mockClaims = new DefaultClaims(claimsMap);

        when(jwtValidator.validate("token")).thenReturn(mockClaims);

        webTestClient.get().uri("/api/v1/admin/dashboard")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldAllowAuthorizedRequest() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("role", "ADMIN");
        Claims mockClaims = new DefaultClaims(claimsMap);

        when(jwtValidator.validate("token")).thenReturn(mockClaims);

        webTestClient.get().uri("/api/v1/admin/dashboard")
                .header("Authorization", "Bearer token")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldAllowPublicEndpointWithoutAuthentication() {
        webTestClient.post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"test\",\"password\":\"test\"}")
                .exchange()
                .expectStatus().isNotFound();
    }
}