package com.di2it.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Test-only security config: provides a ReactiveJwtDecoder that accepts any token
 * and returns a valid JWT so gateway security tests run offline without real auth-service.
 */
@Configuration
@Profile("test")
public class TestSecurityConfig {

    @Bean
    @Primary
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return token -> Mono.just(validJwt());
    }

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
}
