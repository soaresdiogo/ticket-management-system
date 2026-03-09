package com.di2it.auth_service.application;

import lombok.Builder;
import lombok.Value;

/**
 * Result of the get-public-key use case: PEM and metadata for JWT validation.
 */
@Value
@Builder
public class PublicKeyInfo {

    /**
     * Public key in PEM format (X.509).
     */
    String publicKeyPem;

    /**
     * Key identifier (e.g. for key rotation).
     */
    String keyId;

    /**
     * Algorithm used for signing (e.g. RS256).
     */
    String algorithm;
}
