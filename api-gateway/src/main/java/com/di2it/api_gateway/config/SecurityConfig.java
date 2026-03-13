package com.di2it.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for the API Gateway.
 * Public paths: auth (login, MFA, refresh, public-key, registration), WebSocket handshake (/ws/**), and actuator.
 * All other requests require a valid JWT (validated using auth-service public key).
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/auth/login", "/auth/tenants/*/users", "/auth/public-key",
                                "/auth/verify-mfa", "/auth/refresh", "/auth/logout",
                                "/actuator/**", "/ws/**")
                        .permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
}
