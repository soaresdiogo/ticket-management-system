package com.di2it.api_gateway.infrastructure.jwt;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PemKeyParser")
class PemKeyParserTest {

    private static String VALID_PEM;

    @BeforeAll
    static void generateValidPem() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        String b64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
                .encodeToString(pair.getPublic().getEncoded());
        VALID_PEM = "-----BEGIN PUBLIC KEY-----\n" + b64 + "\n-----END PUBLIC KEY-----";
    }

    @Nested
    @DisplayName("parsePublicKey")
    class ParsePublicKey {

        @Test
        @DisplayName("returns RSAPublicKey for valid PEM")
        void validPem_returnsRsaPublicKey() {
            RSAPublicKey key = PemKeyParser.parsePublicKey(VALID_PEM);

            assertThat(key).isNotNull();
            assertThat(key.getAlgorithm()).isEqualTo("RSA");
        }

        @Test
        @DisplayName("accepts PEM with extra whitespace")
        void pemWithWhitespace_accepts() {
            String withSpaces = "  " + VALID_PEM.trim() + "  \n";
            RSAPublicKey key = PemKeyParser.parsePublicKey(withSpaces);

            assertThat(key).isNotNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "   ")
        @DisplayName("throws IllegalArgumentException for null or blank PEM")
        void nullOrBlank_throws(String pem) {
            assertThatThrownBy(() -> PemKeyParser.parsePublicKey(pem))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PEM must not be null or blank");
        }

        @Test
        @DisplayName("throws IllegalArgumentException for invalid Base64 PEM")
        void invalidBase64_throws() {
            String invalid = "-----BEGIN PUBLIC KEY-----\nnot-valid-base64!!!\n-----END PUBLIC KEY-----";

            assertThatThrownBy(() -> PemKeyParser.parsePublicKey(invalid))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws IllegalArgumentException for non-PEM text")
        void nonPemText_throws() {
            assertThatThrownBy(() -> PemKeyParser.parsePublicKey("not a key at all"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
