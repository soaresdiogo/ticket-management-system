package com.di2it.api_gateway.infrastructure.jwt;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Parses PEM-encoded X.509 public keys to {@link RSAPublicKey} for JWT validation (RS256).
 */
public final class PemKeyParser {

    private static final String ALGORITHM = "RSA";
    private static final String BEGIN = "-----BEGIN PUBLIC KEY-----";
    private static final String END = "-----END PUBLIC KEY-----";

    private PemKeyParser() {
    }

    /**
     * Converts a PEM string (X.509 public key) to an RSAPublicKey.
     *
     * @param pem PEM content including BEGIN/END markers
     * @return the RSA public key
     * @throws IllegalArgumentException if the PEM is invalid or not RSA
     */
    public static RSAPublicKey parsePublicKey(String pem) {
        if (pem == null || pem.isBlank()) {
            throw new IllegalArgumentException("PEM must not be null or blank");
        }
        String content = pem
                .replace(BEGIN, "")
                .replace(END, "")
                .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(content);
        try {
            PublicKey key = KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(decoded));
            if (!(key instanceof RSAPublicKey)) {
                throw new IllegalArgumentException("Key is not an RSA public key");
            }
            return (RSAPublicKey) key;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid public key PEM", e);
        }
    }
}
