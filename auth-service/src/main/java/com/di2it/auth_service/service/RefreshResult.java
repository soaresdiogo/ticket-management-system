package com.di2it.auth_service.service;

import lombok.Builder;
import lombok.Value;

/**
 * Result of a successful refresh: new access token and new refresh token (rotation).
 */
@Value
@Builder
public class RefreshResult {

    String accessToken;
    long expiresInSeconds;
    String refreshToken;
}
