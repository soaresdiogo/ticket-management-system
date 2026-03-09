package com.di2it.auth_service.web.mapper;

import com.di2it.auth_service.service.RefreshResult;
import com.di2it.auth_service.web.dto.RefreshResponse;

/**
 * Maps use-case result to HTTP response DTO for the refresh flow.
 */
public final class RefreshResponseMapper {

    private RefreshResponseMapper() {
    }

    public static RefreshResponse toResponse(RefreshResult result) {
        return RefreshResponse.builder()
            .accessToken(result.getAccessToken())
            .tokenType(RefreshResponse.TOKEN_TYPE_BEARER)
            .expiresIn(result.getExpiresInSeconds())
            .refreshToken(result.getRefreshToken())
            .build();
    }
}
