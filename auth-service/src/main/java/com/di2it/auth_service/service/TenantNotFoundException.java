package com.di2it.auth_service.service;

/**
 * Thrown when a tenant is not found by id.
 */
public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(String message) {
        super(message);
    }
}
