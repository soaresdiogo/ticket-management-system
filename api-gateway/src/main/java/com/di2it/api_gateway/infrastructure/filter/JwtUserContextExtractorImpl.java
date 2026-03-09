package com.di2it.api_gateway.infrastructure.filter;

import com.di2it.api_gateway.application.domain.PropagatedUserHeaders;
import com.di2it.api_gateway.application.port.JwtUserContextExtractor;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Extracts user context from JWT claims issued by auth-service.
 * Claims: sub (userId), role, tenantId.
 */
@Component
public class JwtUserContextExtractorImpl implements JwtUserContextExtractor {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TENANT_ID = "tenantId";

    @Override
    public Optional<PropagatedUserHeaders> extract(Jwt jwt) {
        String userId = jwt.getSubject();
        String role = jwt.getClaimAsString(CLAIM_ROLE);
        String tenantId = jwt.getClaimAsString(CLAIM_TENANT_ID);

        if (userId == null || userId.isBlank() || role == null || role.isBlank()
            || tenantId == null || tenantId.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new PropagatedUserHeaders(userId, role, tenantId));
    }
}
