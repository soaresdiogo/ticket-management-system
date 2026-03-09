package com.di2it.auth_service.service;

import com.di2it.auth_service.application.AccessTokenClaims;
import com.di2it.auth_service.application.port.JwtTokenIssuer;
import com.di2it.auth_service.application.port.RefreshTokenCreator;
import com.di2it.auth_service.application.port.RefreshTokenRedeemer;
import com.di2it.auth_service.config.JwtKeyProperties;
import com.di2it.auth_service.domain.entity.User;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Use case: redeem refresh token, issue new access token and new refresh token (rotation).
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRedeemer refreshTokenRedeemer;
    private final JwtTokenIssuer jwtTokenIssuer;
    private final RefreshTokenCreator refreshTokenCreator;
    private final JwtKeyProperties jwtKeyProperties;

    /**
     * Validates the refresh token, revokes it, and issues a new access token and a new refresh token.
     *
     * @param rawRefreshToken the opaque refresh token from the client
     * @return result with new access token, expiry, and new refresh token
     * @throws InvalidRefreshTokenException when the token is invalid, expired, or already used
     */
    public RefreshResult refresh(String rawRefreshToken) {
        User user = refreshTokenRedeemer.redeem(rawRefreshToken);

        if (!user.isActive()) {
            throw new InvalidRefreshTokenException("Invalid or expired refresh token.");
        }

        AccessTokenClaims claims = buildClaims(user);
        long accessExpiry = jwtKeyProperties.getAccessTokenExpirySeconds();
        String accessToken = jwtTokenIssuer.createAccessToken(claims, accessExpiry);
        String newRefreshToken = refreshTokenCreator.create(user, jwtKeyProperties.getRefreshTokenExpirySeconds());

        return RefreshResult.builder()
            .accessToken(accessToken)
            .expiresInSeconds(accessExpiry)
            .refreshToken(newRefreshToken)
            .build();
    }

    private static AccessTokenClaims buildClaims(User user) {
        return AccessTokenClaims.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole())
            .tenantId(user.getTenant().getId())
            .build();
    }
}
