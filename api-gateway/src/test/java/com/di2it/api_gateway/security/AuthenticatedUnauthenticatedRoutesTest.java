package com.di2it.api_gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Verifies that the API Gateway allows unauthenticated access only to public paths
 * and requires a valid JWT for all other routes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Authenticated vs unauthenticated routes")
class AuthenticatedUnauthenticatedRoutesTest {

    private static final String VALID_BEARER_TOKEN = "Bearer valid-jwt-token";

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @MockitoBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    private static Jwt validJwt() {
        return Jwt.withTokenValue("valid-jwt-token")
                .header("alg", "RS256")
                .subject("user-123")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("role", "CLIENT")
                .claim("tenantId", "tenant-456")
                .build();
    }

    @BeforeEach
    void setUp() {
        when(reactiveJwtDecoder.decode(anyString())).thenReturn(Mono.just(validJwt()));
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Nested
    @DisplayName("Unauthenticated requests")
    class Unauthenticated {

        @Test
        @DisplayName("public key is accessible without token")
        void publicKeyAllowedWithoutToken() {
            webTestClient.get()
                    .uri("/auth/public-key")
                    .exchange()
                    .expectStatus().value(v -> assertThat(v).isNotEqualTo(401));
        }

        @Test
        @DisplayName("login is accessible without token")
        void loginAllowedWithoutToken() {
            webTestClient.post()
                    .uri("/auth/login")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue("{}")
                    .exchange()
                    .expectStatus().value(v -> assertThat(v).isNotEqualTo(401));
        }

        @Test
        @DisplayName("tenant user registration is accessible without token")
        void tenantUserRegistrationAllowedWithoutToken() {
            webTestClient.post()
                    .uri("/auth/tenants/550e8400-e29b-41d4-a716-446655440000/users")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue("{}")
                    .exchange()
                    .expectStatus().value(v -> assertThat(v).isNotEqualTo(401));
        }

        @Test
        @DisplayName("actuator health is accessible without token")
        void actuatorHealthAllowedWithoutToken() {
            webTestClient.get()
                    .uri("/actuator/health")
                    .exchange()
                    .expectStatus().value(v -> assertThat(v).isNotEqualTo(401));
        }

        @Test
        @DisplayName("tickets path returns 401 without token")
        void ticketsRequiresAuth() {
            webTestClient.get()
                    .uri("/tickets/1")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("auth change-password returns 401 without token")
        void changePasswordRequiresAuth() {
            webTestClient.post()
                    .uri("/auth/change-password")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue("{}")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("files path returns 401 without token")
        void filesRequiresAuth() {
            webTestClient.get()
                    .uri("/files/1/download")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("notifications path returns 401 without token")
        void notificationsRequiresAuth() {
            webTestClient.get()
                    .uri("/notifications")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }

    @Nested
    @DisplayName("Authenticated requests")
    class Authenticated {

        @Test
        @DisplayName("tickets path is accessible with valid JWT")
        void ticketsAllowedWithToken() {
            webTestClient.get()
                    .uri("/tickets/1")
                    .header("Authorization", VALID_BEARER_TOKEN)
                    .exchange()
                    .expectStatus().value(v -> assertThat(v).isNotEqualTo(401));
        }

        @Test
        @DisplayName("auth change-password is accessible with valid JWT")
        void changePasswordAllowedWithToken() {
            webTestClient.post()
                    .uri("/auth/change-password")
                    .header("Authorization", VALID_BEARER_TOKEN)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue("{}")
                    .exchange()
                    .expectStatus().value(v -> assertThat(v).isNotEqualTo(401));
        }

        @Test
        @DisplayName("files path is accessible with valid JWT")
        void filesAllowedWithToken() {
            webTestClient.get()
                    .uri("/files/1/download")
                    .header("Authorization", VALID_BEARER_TOKEN)
                    .exchange()
                    .expectStatus().value(v -> assertThat(v).isNotEqualTo(401));
        }

        @Test
        @DisplayName("notifications path is accessible with valid JWT")
        void notificationsAllowedWithToken() {
            webTestClient.get()
                    .uri("/notifications")
                    .header("Authorization", VALID_BEARER_TOKEN)
                    .exchange()
                    .expectStatus().value(v -> assertThat(v).isNotEqualTo(401));
        }
    }
}
