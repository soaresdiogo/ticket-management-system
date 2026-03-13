package com.di2it.ticket_service.web;

/**
 * HTTP header names propagated by the API Gateway from the validated JWT.
 * Used to obtain user and tenant context without parsing JWT in this service.
 */
public final class WebConstants {

    private WebConstants() {
    }

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";

    /** Role required to list all tickets (tenant-scoped). */
    public static final String ROLE_ACCOUNTANT = "ACCOUNTANT";
    /** Office staff role (same permissions as ACCOUNTANT for list/status). */
    public static final String ROLE_USER = "USER";

    /** True if the role is allowed to use office endpoints (list all tickets, change status). */
    public static boolean isOfficeRole(String role) {
        return role != null && (ROLE_ACCOUNTANT.equals(role) || ROLE_USER.equals(role));
    }
}
