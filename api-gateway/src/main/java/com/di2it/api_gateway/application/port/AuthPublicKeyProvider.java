package com.di2it.api_gateway.application.port;

import reactor.core.publisher.Mono;

/**
 * Port for obtaining the JWT public key (PEM) used to validate tokens.
 * Implementations typically fetch from auth-service and may cache the result.
 */
@FunctionalInterface
public interface AuthPublicKeyProvider {

    /**
     * Returns the current public key in PEM format (X.509) for RS256 JWT validation.
     *
     * @return PEM string including "-----BEGIN PUBLIC KEY-----" / "-----END PUBLIC KEY-----",
     *         or empty Mono if the key cannot be obtained
     */
    Mono<String> getPublicKeyPem();
}
