package com.di2it.auth_service.web.mapper;

import com.di2it.auth_service.application.PublicKeyInfo;
import com.di2it.auth_service.web.dto.PublicKeyResponse;

/**
 * Maps PublicKeyInfo (use-case result) to HTTP response DTO for the public-key endpoint.
 */
public final class PublicKeyResponseMapper {

    private PublicKeyResponseMapper() {
    }

    public static PublicKeyResponse toResponse(PublicKeyInfo info) {
        return PublicKeyResponse.builder()
            .publicKey(info.getPublicKeyPem())
            .keyId(info.getKeyId())
            .algorithm(info.getAlgorithm())
            .build();
    }
}
