package com.di2it.auth_service.service;

import com.di2it.auth_service.application.PublicKeyInfo;
import com.di2it.auth_service.application.port.JwtPublicKeyProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPublicKeyServiceTest {

    private static final String PEM =
        "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...\n-----END PUBLIC KEY-----";
    private static final String KEY_ID = "tms-auth";

    @Mock
    private JwtPublicKeyProvider jwtPublicKeyProvider;

    @InjectMocks
    private GetPublicKeyService getPublicKeyService;

    @BeforeEach
    void setUp() {
        when(jwtPublicKeyProvider.getPublicKeyPem()).thenReturn(PEM);
        when(jwtPublicKeyProvider.getKeyId()).thenReturn(KEY_ID);
    }

    @Nested
    @DisplayName("getPublicKey")
    class GetPublicKey {

        @Test
        @DisplayName("returns PublicKeyInfo with PEM, keyId and RS256 algorithm")
        void success() {
            PublicKeyInfo result = getPublicKeyService.getPublicKey();

            assertThat(result.getPublicKeyPem()).isEqualTo(PEM);
            assertThat(result.getKeyId()).isEqualTo(KEY_ID);
            assertThat(result.getAlgorithm()).isEqualTo("RS256");

            verify(jwtPublicKeyProvider).getPublicKeyPem();
            verify(jwtPublicKeyProvider).getKeyId();
        }

        @Test
        @DisplayName("delegates to provider for PEM and keyId")
        void delegatesToProvider() {
            getPublicKeyService.getPublicKey();

            verify(jwtPublicKeyProvider).getPublicKeyPem();
            verify(jwtPublicKeyProvider).getKeyId();
        }
    }
}
