package com.di2it.auth_service.application.port;

import com.di2it.auth_service.application.AccessTokenClaims;

/**
 * Port for issuing RS256 access tokens (JWT).
 */
@FunctionalInterface
public interface JwtTokenIssuer {

    /**
     * Builds a signed JWT with the given claims and expiry.
     *
     * @param claims        user claims to embed (userId, email, role, tenantId)
     * @param expirySeconds validity in seconds from now
     * @return the compact JWT string
     */
    String createAccessToken(AccessTokenClaims claims, long expirySeconds);
}
