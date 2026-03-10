package com.di2it.auth_service.application.port;

import com.di2it.auth_service.domain.entity.User;

/**
 * Port for creating and persisting a refresh token for a user.
 * Returns the opaque token value to be sent to the client (only once).
 */
@FunctionalInterface
public interface RefreshTokenCreator {

    /**
     * Generates a refresh token, stores its hash in the database, and returns the raw token.
     *
     * @param user           the user to associate the token with
     * @param expirySeconds  validity in seconds from now
     * @return the opaque token string to return to the client
     */
    String create(User user, long expirySeconds);
}
