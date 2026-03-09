package com.di2it.auth_service.service;

/**
 * Thrown when a refresh token is invalid, expired, or already revoked.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
