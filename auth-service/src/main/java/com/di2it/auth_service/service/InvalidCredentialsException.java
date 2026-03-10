package com.di2it.auth_service.service;

/**
 * Thrown when login credentials (email/password) are invalid.
 */
public class InvalidCredentialsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
