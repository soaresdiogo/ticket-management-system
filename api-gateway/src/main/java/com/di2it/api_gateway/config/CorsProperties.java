package com.di2it.api_gateway.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration properties for CORS (Cross-Origin Resource Sharing).
 * Used to allow the Angular frontend (and other configured origins) to call the gateway.
 */
@Data
@ConfigurationProperties(prefix = "gateway.cors")
public class CorsProperties {

    /**
     * Allowed origins (e.g. http://localhost:4200 for Angular dev server).
     * Empty list disables CORS.
     */
    private List<String> allowedOrigins = List.of("http://localhost:4200");

    /**
     * Allowed HTTP methods.
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    /**
     * Allowed request headers. Use "*" to allow all.
     */
    private List<String> allowedHeaders = List.of("*");

    /**
     * Whether credentials (cookies, Authorization header) are allowed.
     */
    private boolean allowCredentials = true;

    /**
     * Max age in seconds for preflight response caching.
     */
    private long maxAgeSeconds = 3600L;
}
