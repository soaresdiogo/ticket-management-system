package com.di2it.api_gateway.application.port;

import com.di2it.api_gateway.application.domain.PropagatedUserHeaders;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * Port for extracting user context from a validated JWT to propagate to downstream services.
 * Implementations read standard claims (sub, role, tenantId) and return a value object
 * suitable for header propagation.
 */
@FunctionalInterface
public interface JwtUserContextExtractor {

    /**
     * Extracts user context from the JWT if all required claims are present.
     *
     * @param jwt validated JWT (subject = userId; claims: role, tenantId)
     * @return propagated headers, or empty if any required claim is missing
     */
    Optional<PropagatedUserHeaders> extract(Jwt jwt);
}
