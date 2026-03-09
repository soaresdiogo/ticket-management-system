package com.di2it.api_gateway.application.domain;

/**
 * Value object holding user context to propagate to downstream services via HTTP headers.
 * Extracted from the validated JWT and added as X-User-Id, X-User-Role, X-Tenant-Id.
 */
public record PropagatedUserHeaders(
    String userId,
    String role,
    String tenantId
) {
    /**
     * Header names used when propagating to downstream services.
     */
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
}
