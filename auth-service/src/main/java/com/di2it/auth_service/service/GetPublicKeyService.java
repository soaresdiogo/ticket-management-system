package com.di2it.auth_service.service;

import com.di2it.auth_service.application.PublicKeyInfo;
import com.di2it.auth_service.application.port.JwtPublicKeyProvider;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Use case: expose public key for gateway and other services to validate JWT (RS256).
 */
@Service
@RequiredArgsConstructor
public class GetPublicKeyService {

    private static final String JWT_ALGORITHM = "RS256";

    private final JwtPublicKeyProvider jwtPublicKeyProvider;

    /**
     * Returns the public key in PEM format plus metadata (keyId, algorithm).
     *
     * @return public key info for JWT validation
     */
    public PublicKeyInfo getPublicKey() {
        return PublicKeyInfo.builder()
            .publicKeyPem(jwtPublicKeyProvider.getPublicKeyPem())
            .keyId(jwtPublicKeyProvider.getKeyId())
            .algorithm(JWT_ALGORITHM)
            .build();
    }
}
