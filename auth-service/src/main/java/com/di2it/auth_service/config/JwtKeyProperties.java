package com.di2it.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for JWT RS256 key pair.
 * Keys can be provided via:
 * <ul>
 *   <li>File paths: {@link #privateKeyPath} and {@link #publicKeyPath}</li>
 *   <li>Environment: AUTH_JWT_PRIVATE_KEY and AUTH_JWT_PUBLIC_KEY (PEM strings)</li>
 *   <li>Auto-generation: if neither is set, keys are generated and stored under {@link #keyDir}</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "auth.jwt")
@Getter
@Setter
public class JwtKeyProperties {

    /**
     * Path to PEM file containing the private key (optional if env or auto-gen is used).
     */
    private String privateKeyPath = "";

    /**
     * Path to PEM file containing the public key (optional if env or auto-gen is used).
     */
    private String publicKeyPath = "";

    /**
     * Directory where keys are generated and stored when no file/env keys are provided.
     */
    private String keyDir = "keys";

    /**
     * Key id / alias used in JWT headers (optional).
     */
    private String keyId = "tms-auth";
}
