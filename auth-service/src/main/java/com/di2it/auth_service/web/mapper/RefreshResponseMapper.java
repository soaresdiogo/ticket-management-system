package com.di2it.auth_service.web.mapper;

import com.di2it.auth_service.service.RefreshResult;
import com.di2it.auth_service.web.dto.RefreshResponse;

/**
 * Maps use-case result to HTTP response DTO for the refresh flow.
 */
public final class RefreshResponseMapper {

    private RefreshResponseMapper() {
    }

    /**
     * Maps to response DTO. Refresh token is set in HttpOnly cookie by the controller, not in body.
     */
    public static RefreshResponse toResponse(RefreshResult result) {
        return RefreshResponse.builder()
            .accessToken(result.getAccessToken())
            .tokenType(RefreshResponse.TOKEN_TYPE_BEARER)
            .expiresIn(result.getExpiresInSeconds())
            .refreshTokenSet(true)
            .build();
    }
}
