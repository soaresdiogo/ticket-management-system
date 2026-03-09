package com.di2it.api_gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration properties for the auth-service (e.g. base URL for fetching JWT public key).
 */
@Data
@ConfigurationProperties(prefix = "auth.service")
public class AuthServiceProperties {

    /**
     * Base URL of the auth-service (e.g. http://localhost:8081).
     */
    private String url = "http://localhost:8081";
}
