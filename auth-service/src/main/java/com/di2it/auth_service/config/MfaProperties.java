package com.di2it.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for MFA (code length and Redis TTL).
 */
@ConfigurationProperties(prefix = "auth.mfa")
@Getter
@Setter
public class MfaProperties {

    /**
     * Length of the numeric MFA code (e.g. 6 for "123456").
     */
    private int codeLength = 6;

    /**
     * TTL in seconds for the MFA code stored in Redis.
     */
    private long ttlSeconds = 300;
}
