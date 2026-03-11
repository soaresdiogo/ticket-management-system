package com.di2it.notification_service.infrastructure.auth;

import com.di2it.notification_service.application.port.ResolveUserEmailPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

/**
 * Resolves user email by calling auth-service internal API GET /auth/internal/users/{id}.
 */
@Component
public class AuthServiceUserEmailResolver implements ResolveUserEmailPort {

    private final RestClient restClient;

    public AuthServiceUserEmailResolver(
        @Value("${notification-service.auth-service.base-url:http://localhost:8081}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .build();
    }

    @Override
    public Optional<String> resolveEmail(UUID userId) {
        try {
            AuthUserResponse response = restClient.get()
                .uri("/auth/internal/users/{id}", userId)
                .retrieve()
                .body(AuthUserResponse.class);
            return response != null && response.email() != null
                ? Optional.of(response.email())
                : Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private record AuthUserResponse(UUID id, String email) {
    }
}
