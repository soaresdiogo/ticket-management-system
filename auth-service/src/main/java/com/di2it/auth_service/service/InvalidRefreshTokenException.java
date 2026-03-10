package com.di2it.auth_service.service;

/**
 * Thrown when a refresh token is invalid, expired, or already revoked.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
