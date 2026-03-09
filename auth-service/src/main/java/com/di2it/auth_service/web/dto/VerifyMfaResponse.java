package com.di2it.auth_service.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyMfaResponse {

    public static final String TOKEN_TYPE_BEARER = "Bearer";

    /**
     * RS256 signed JWT access token.
     */
    private String accessToken;

    /**
     * Token type for Authorization header (e.g. "Bearer").
     */
    private String tokenType;

    /**
     * Access token validity in seconds.
     */
    private long expiresIn;

    /**
     * Opaque refresh token; present only when requested. Use with POST /auth/refresh.
     */
    private String refreshToken;
}
