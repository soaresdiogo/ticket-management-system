package com.di2it.api_gateway.security;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the API Gateway allows unauthenticated access only to public paths
 * and requires a valid JWT for all other routes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Authenticated vs unauthenticated routes")
class AuthenticatedUnauthenticatedRoutesTest {

    private static final String VALID_JWT_TOKEN = "valid-jwt-token";

    private static WireMockServer wireMock;

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(0);
        wireMock.start();
        // Stub auth endpoints so gateway can proxy without real auth-service (offline)
        wireMock.stubFor(post(urlPathMatching("/auth/login")).willReturn(aResponse().withStatus(200)));
        wireMock.stubFor(post(urlPathMatching("/auth/tenants/.*/users"))
                .willReturn(aResponse().withStatus(200)));
        wireMock.stubFor(get(urlPathMatching("/auth/public-key"))
                .willReturn(aResponse().withStatus(200).withBody("{}")));
        wireMock.stubFor(post(urlPathMatching("/auth/change-password")).willReturn(aResponse().withStatus(200)));
        // Stub other services so authenticated tests get 200 from backend
        wireMock.stubFor(any(anyUrl()).willReturn(aResponse().withStatus(200)));
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    @DynamicPropertySource
    static void setServiceUrls(DynamicPropertyRegistry registry) {
        String base = "http://localhost:" + wireMock.port();
        registry.add("AUTH_SERVICE_URL", () -> base);
        registry.add("TICKET_SERVICE_URL", () -> base);
        registry.add("FILE_SERVICE_URL", () -> base);
        registry.add("NOTIFICATION_SERVICE_URL", () -> base);
    }

    @BeforeEach
    void setUp() {
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
                    .headers(h -> h.setBearerAuth(VALID_JWT_TOKEN))
                    .exchange()
                    .expectStatus().value(v -> assertThat(v).isNotEqualTo(401));
        }

        @Test
        @DisplayName("auth change-password is accessible with valid JWT")
        void changePasswordAllowedWithToken() {
            webTestClient.post()
                    .uri("/auth/change-password")
                    .headers(h -> h.setBearerAuth(VALID_JWT_TOKEN))
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
                    .headers(h -> h.setBearerAuth(VALID_JWT_TOKEN))
                    .exchange()
                    .expectStatus().value(v -> assertThat(v).isNotEqualTo(401));
        }

        @Test
        @DisplayName("notifications path is accessible with valid JWT")
        void notificationsAllowedWithToken() {
            webTestClient.get()
                    .uri("/notifications")
                    .headers(h -> h.setBearerAuth(VALID_JWT_TOKEN))
                    .exchange()
                    .expectStatus().value(v -> assertThat(v).isNotEqualTo(401));
        }
    }
}
