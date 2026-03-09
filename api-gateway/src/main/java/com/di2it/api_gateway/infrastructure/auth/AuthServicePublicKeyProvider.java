package com.di2it.api_gateway.infrastructure.auth;

import com.di2it.api_gateway.application.port.AuthPublicKeyProvider;
import com.di2it.api_gateway.config.AuthServiceProperties;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

/**
 * Fetches the JWT public key from auth-service and caches it for the lifetime of the provider.
 * Uses auth-service GET /auth/public-key.
 */
@Component
public class AuthServicePublicKeyProvider implements AuthPublicKeyProvider {

    private final WebClient webClient;
    private final String publicKeyUri;
    private volatile String cachedPem;
    private static final String PUBLIC_KEY_PATH = "/auth/public-key";

    public AuthServicePublicKeyProvider(AuthServiceProperties properties, WebClient.Builder webClientBuilder) {
        this.publicKeyUri = properties.getUrl().replaceAll("/$", "") + PUBLIC_KEY_PATH;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<String> getPublicKeyPem() {
        if (cachedPem != null) {
            return Mono.just(cachedPem);
        }
        return webClient.get()
                .uri(publicKeyUri)
                .retrieve()
                .bodyToMono(PublicKeyResponseDto.class)
                .map(dto -> {
                    if (dto == null || dto.getPublicKey() == null || dto.getPublicKey().isBlank()) {
                        throw new IllegalStateException("Auth service returned empty public key");
                    }
                    cachedPem = dto.getPublicKey();
                    return cachedPem;
                })
                .onErrorMap(WebClientResponseException.class, e ->
                    new IllegalStateException("Failed to fetch public key from auth-service: " + e.getStatusCode(), e));
    }
}
