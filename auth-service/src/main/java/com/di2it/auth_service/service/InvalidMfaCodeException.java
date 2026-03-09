package com.di2it.auth_service.service;

/**
 * Thrown when the provided MFA code is invalid, expired, or does not match the stored value.
 */
public class InvalidMfaCodeException extends RuntimeException {

    public InvalidMfaCodeException(String message) {
        super(message);
    }
}
