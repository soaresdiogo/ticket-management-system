package com.di2it.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for the refresh-token HTTP-only cookie.
 * Used to avoid storing refresh tokens in client storage (e.g. localStorage).
 */
@ConfigurationProperties(prefix = "auth.cookie")
@Getter
@Setter
public class CookieProperties {

    /**
     * Cookie name for the refresh token. Default: tms_refresh_token.
     */
    private String refreshTokenName = "tms_refresh_token";

    /**
     * Cookie path. Default: /.
     */
    private String path = "/";

    /**
     * Whether the cookie is HTTP-only (not accessible via JavaScript). Default: true.
     */
    private boolean httpOnly = true;

    /**
     * Whether the cookie is sent only over HTTPS. Default: true; set false for local HTTP.
     */
    private boolean secure = true;

    /**
     * SameSite attribute: Lax, Strict, or None. Default: Lax.
     */
    private String sameSite = "Lax";
}
