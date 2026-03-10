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
}
