package com.di2it.auth_service.service;

import lombok.Builder;
import lombok.Value;

/**
 * Result of successful MFA verification: access token and optional refresh token.
 */
@Value
@Builder
public class VerifyMfaResult {

    String accessToken;
    long expiresInSeconds;
    String refreshToken;

    /**
     * @return true if a refresh token was issued
     */
    public boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.isBlank();
    }
}
