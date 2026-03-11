package com.di2it.notification_service.web;

/**
 * HTTP header names propagated by the API Gateway from the validated JWT.
 */
public final class WebConstants {

    private WebConstants() {
    }

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
}
