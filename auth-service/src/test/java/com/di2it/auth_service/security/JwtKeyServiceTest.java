package com.di2it.auth_service.security;

import com.di2it.auth_service.config.JwtKeyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtKeyServiceTest {

    @Mock
    private JwtKeyProperties properties;

    @Mock
    private Environment environment;

    @Mock
    private ResourceLoader resourceLoader;

    private JwtKeyService jwtKeyService;

    @BeforeEach
    void setUp() {
        when(properties.getKeyId()).thenReturn("test-key-id");
        when(properties.getPrivateKeyPath()).thenReturn("");
        when(properties.getPublicKeyPath()).thenReturn("");
        when(properties.getKeyDir()).thenReturn("target/test-keys");
        when(environment.getProperty("AUTH_JWT_PRIVATE_KEY")).thenReturn(null);
        when(environment.getProperty("AUTH_JWT_PUBLIC_KEY")).thenReturn(null);
    }

    @Nested
    @DisplayName("getKeyId")
    class GetKeyId {

        @Test
        @DisplayName("returns key id from properties")
        void returnsKeyIdFromProperties() {
            jwtKeyService = new JwtKeyService(properties, environment, resourceLoader);
            jwtKeyService.init();

            assertThat(jwtKeyService.getKeyId()).isEqualTo("test-key-id");
        }
    }

    @Nested
    @DisplayName("init from environment PEM")
    class InitFromEnvironment {

        @Test
        @DisplayName("loads keys from environment when AUTH_JWT_PRIVATE_KEY and AUTH_JWT_PUBLIC_KEY are set")
        void loadsFromEnv() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair pair = gen.generateKeyPair();
            String privatePem = toPemPrivate(pair.getPrivate());
            String publicPem = toPemPublic(pair.getPublic());

            when(environment.getProperty("AUTH_JWT_PRIVATE_KEY")).thenReturn(privatePem);
            when(environment.getProperty("AUTH_JWT_PUBLIC_KEY")).thenReturn(publicPem);

            jwtKeyService = new JwtKeyService(properties, environment, resourceLoader);
            jwtKeyService.init();

            assertThat(jwtKeyService.getPrivateKey()).isNotNull();
            assertThat(jwtKeyService.getPublicKey()).isNotNull();
            assertThat(jwtKeyService.getPrivateKey().getAlgorithm()).isEqualTo("RSA");
            assertThat(jwtKeyService.getPublicKey().getAlgorithm()).isEqualTo("RSA");
        }

        @Test
        @DisplayName("accepts Base64-encoded PEM from environment")
        void acceptsBase64EncodedPem() throws Exception {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair pair = gen.generateKeyPair();
            String privatePem = toPemPrivate(pair.getPrivate());
            String publicPem = toPemPublic(pair.getPublic());
            String privateB64 = Base64.getEncoder().encodeToString(privatePem.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String publicB64 = Base64.getEncoder().encodeToString(publicPem.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            when(environment.getProperty("AUTH_JWT_PRIVATE_KEY")).thenReturn(privateB64);
            when(environment.getProperty("AUTH_JWT_PUBLIC_KEY")).thenReturn(publicB64);

            jwtKeyService = new JwtKeyService(properties, environment, resourceLoader);
            jwtKeyService.init();

            assertThat(jwtKeyService.getPrivateKey()).isNotNull();
            assertThat(jwtKeyService.getPublicKey()).isNotNull();
        }
    }

    private static String toPemPrivate(PrivateKey key) {
        String b64 = Base64.getMimeEncoder(64, "\n".getBytes(java.nio.charset.StandardCharsets.UTF_8))
            .encodeToString(key.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" + b64 + "\n-----END PRIVATE KEY-----";
    }

    private static String toPemPublic(PublicKey key) {
        String b64 = Base64.getMimeEncoder(64, "\n".getBytes(java.nio.charset.StandardCharsets.UTF_8))
            .encodeToString(key.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + b64 + "\n-----END PUBLIC KEY-----";
    }
}
