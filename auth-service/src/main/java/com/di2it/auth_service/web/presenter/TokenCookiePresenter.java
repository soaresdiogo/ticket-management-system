package com.di2it.auth_service.web.presenter;

import com.di2it.auth_service.config.CookieProperties;

import org.springframework.http.ResponseCookie;

/**
 * Builds HttpOnly cookie for the refresh token (set or clear).
 * Single responsibility: cookie construction for security (no token in response body).
 */
public final class TokenCookiePresenter {

    private static final long CLEAR_MAX_AGE = 0L;

    private TokenCookiePresenter() {
    }

    /**
     * Builds a Set-Cookie for the refresh token (HttpOnly, Secure, SameSite).
     *
     * @param refreshTokenValue the opaque refresh token value
     * @param maxAgeSeconds     cookie max-age in seconds
     * @param cookieProps       cookie configuration
     * @return the cookie to add to the response
     */
    public static ResponseCookie setRefreshTokenCookie(
        String refreshTokenValue,
        long maxAgeSeconds,
        CookieProperties cookieProps
    ) {
        return ResponseCookie.from(cookieProps.getRefreshTokenName(), refreshTokenValue)
            .path(cookieProps.getPath())
            .maxAge(maxAgeSeconds)
            .httpOnly(cookieProps.isHttpOnly())
            .secure(cookieProps.isSecure())
            .sameSite(cookieProps.getSameSite())
            .build();
    }

    /**
     * Builds a Set-Cookie that clears the refresh token (max-age=0, same name/path).
     *
     * @param cookieProps cookie configuration (name and path must match the set cookie)
     * @return the cookie to add to the response to clear the refresh token
     */
    public static ResponseCookie clearRefreshTokenCookie(CookieProperties cookieProps) {
        return ResponseCookie.from(cookieProps.getRefreshTokenName(), "")
            .path(cookieProps.getPath())
            .maxAge(CLEAR_MAX_AGE)
            .httpOnly(cookieProps.isHttpOnly())
            .secure(cookieProps.isSecure())
            .sameSite(cookieProps.getSameSite())
            .build();
    }
}
