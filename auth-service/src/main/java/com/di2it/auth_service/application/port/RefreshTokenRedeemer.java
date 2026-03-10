package com.di2it.auth_service.application.port;

import com.di2it.auth_service.domain.entity.User;

/**
 * Port for redeeming a refresh token: validate, revoke (one-time use), and return the associated user.
 */
@FunctionalInterface
public interface RefreshTokenRedeemer {

    /**
     * Validates the refresh token (exists, not revoked, not expired), revokes it, and returns the user.
     *
     * @param rawToken the opaque refresh token from the client
     * @return the user associated with the token
     * @throws com.di2it.auth_service.service.InvalidRefreshTokenException when token is invalid, expired, or revoked
     */
    User redeem(String rawToken);
}
