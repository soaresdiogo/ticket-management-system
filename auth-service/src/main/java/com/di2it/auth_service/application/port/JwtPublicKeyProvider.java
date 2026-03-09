package com.di2it.auth_service.application.port;

/**
 * Port for providing the JWT public key in PEM format.
 * Used by the gateway and other services to validate RS256 JWTs issued by auth-service.
 */
public interface JwtPublicKeyProvider {

    /**
     * Returns the public key in PEM format (X.509, suitable for NimbusJwtDecoder / Spring).
     *
     * @return PEM string including "-----BEGIN PUBLIC KEY-----" and "-----END PUBLIC KEY-----"
     */
    String getPublicKeyPem();

    /**
     * Returns the key identifier used in JWT headers (e.g. for key rotation).
     *
     * @return key id, never null
     */
    String getKeyId();
}
