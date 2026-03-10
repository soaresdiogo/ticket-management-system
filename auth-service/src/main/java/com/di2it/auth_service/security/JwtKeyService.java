package com.di2it.auth_service.security;

import com.di2it.auth_service.application.port.JwtPublicKeyProvider;
import com.di2it.auth_service.config.JwtKeyProperties;

import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Loads or generates the RSA key pair used for JWT signing (RS256).
 * Priority: 1) Env PEM strings (raw or Base64-encoded), 2) Key files, 3) Generate and store under keyDir.
 * Env vars AUTH_JWT_PRIVATE_KEY and AUTH_JWT_PUBLIC_KEY accept either raw PEM or Base64-encoded PEM.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtKeyService implements JwtPublicKeyProvider {

    private static final String ENV_PRIVATE_KEY = "AUTH_JWT_PRIVATE_KEY";
    private static final String ENV_PUBLIC_KEY = "AUTH_JWT_PUBLIC_KEY";
    private static final int KEY_SIZE = 2048;
    private static final String ALGORITHM = "RSA";

    private final JwtKeyProperties properties;
    private final Environment environment;
    private final ResourceLoader resourceLoader;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        String privatePem = environment.getProperty(ENV_PRIVATE_KEY);
        String publicPem = environment.getProperty(ENV_PUBLIC_KEY);

        if (privatePem != null && !privatePem.isBlank() && publicPem != null && !publicPem.isBlank()) {
            if (log.isInfoEnabled()) {
                log.info("Loading JWT keys from environment (AUTH_JWT_PRIVATE_KEY / AUTH_JWT_PUBLIC_KEY)");
            }
            privateKey = parsePrivateKey(decodePemFromEnv(privatePem));
            publicKey = parsePublicKey(decodePemFromEnv(publicPem));
            return;
        }

        String privatePath = properties.getPrivateKeyPath();
        String publicPath = properties.getPublicKeyPath();
        if (privatePath != null && !privatePath.isBlank() && publicPath != null && !publicPath.isBlank()) {
            if (log.isInfoEnabled()) {
                log.info("Loading JWT keys from files: {} and {}", privatePath, publicPath);
            }
            privateKey = loadPrivateKeyFromPath(privatePath);
            publicKey = loadPublicKeyFromPath(publicPath);
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("No JWT keys configured; generating and storing in {}", properties.getKeyDir());
        }
        KeyPair pair = generateAndStoreKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public String getKeyId() {
        return properties.getKeyId();
    }

    @Override
    public String getPublicKeyPem() {
        return toPemPublic(publicKey);
    }

    private KeyPair generateAndStoreKeyPair() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM);
            gen.initialize(KEY_SIZE);
            KeyPair pair = gen.generateKeyPair();

            Path dir = Path.of(properties.getKeyDir()).toAbsolutePath();
            Files.createDirectories(dir);

            Path privateFile = dir.resolve("private.pem");
            Path publicFile = dir.resolve("public.pem");

            String privatePem = toPemPrivate(pair.getPrivate());
            String publicPem = toPemPublic(pair.getPublic());
            Files.writeString(privateFile, privatePem);
            Files.writeString(publicFile, publicPem);

            if (log.isInfoEnabled()) {
                log.info("Generated and saved RSA key pair to {} and {}", privateFile, publicFile);
            }
            return pair;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate or store JWT key pair", e);
        }
    }

    private PrivateKey loadPrivateKeyFromPath(String path) {
        String pem = readResourceOrFile(path);
        return parsePrivateKey(pem);
    }

    private PublicKey loadPublicKeyFromPath(String path) {
        String pem = readResourceOrFile(path);
        return parsePublicKey(pem);
    }

    private String readResourceOrFile(String path) {
        try {
            Resource resource = resourceLoader.getResource(path.startsWith("file:") ? path : "file:" + path);
            if (!resource.exists()) {
                resource = resourceLoader.getResource(path);
            }
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read key from path: " + path, e);
        }
    }

    /**
     * If the env value is Base64-encoded PEM, decode it; otherwise return as-is (raw PEM).
     */
    private static String decodePemFromEnv(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String trimmed = value.trim();
        if (trimmed.contains("-----BEGIN")) {
            return trimmed;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(trimmed), StandardCharsets.UTF_8);
            return decoded.contains("-----BEGIN") ? decoded : trimmed;
        } catch (IllegalArgumentException _) {
            return trimmed;
        }
    }

    private static PrivateKey parsePrivateKey(String pem) {
        try {
            String content = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(content);
            return KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid private key PEM", e);
        }
    }

    private static PublicKey parsePublicKey(String pem) {
        try {
            String content = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(content);
            return KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(decoded));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid public key PEM", e);
        }
    }

    private static String toPemPrivate(PrivateKey key) {
        String b64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
            .encodeToString(key.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" + b64 + "\n-----END PRIVATE KEY-----";
    }

    private static String toPemPublic(PublicKey key) {
        String b64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
            .encodeToString(key.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + b64 + "\n-----END PUBLIC KEY-----";
    }
}
