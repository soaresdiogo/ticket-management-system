package com.di2it.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for the API Gateway.
 * Public paths: auth (login, MFA, refresh, public-key, registration) and actuator health.
 * All other requests require a valid JWT (validated using auth-service public key).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/auth/public-key").permitAll()
                        .pathMatchers("/auth/login").permitAll()
                        .pathMatchers("/auth/verify-mfa").permitAll()
                        .pathMatchers("/auth/refresh").permitAll()
                        .pathMatchers("/auth/tenants/*/users").permitAll()
                        .pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
                .build();
    }
}
