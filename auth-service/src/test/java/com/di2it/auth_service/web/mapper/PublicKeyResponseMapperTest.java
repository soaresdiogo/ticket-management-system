package com.di2it.auth_service.web.mapper;

import com.di2it.auth_service.application.PublicKeyInfo;
import com.di2it.auth_service.web.dto.PublicKeyResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PublicKeyResponseMapperTest {

    private static final String PEM = "-----BEGIN PUBLIC KEY-----\nMIIBIjAN...\n-----END PUBLIC KEY-----";
    private static final String KEY_ID = "tms-auth";
    private static final String ALGORITHM = "RS256";

    @Test
    @DisplayName("maps PublicKeyInfo to PublicKeyResponse with all fields")
    void toResponse() {
        PublicKeyInfo info = PublicKeyInfo.builder()
            .publicKeyPem(PEM)
            .keyId(KEY_ID)
            .algorithm(ALGORITHM)
            .build();

        PublicKeyResponse response = PublicKeyResponseMapper.toResponse(info);

        assertThat(response.getPublicKey()).isEqualTo(PEM);
        assertThat(response.getKeyId()).isEqualTo(KEY_ID);
        assertThat(response.getAlgorithm()).isEqualTo(ALGORITHM);
    }
}
