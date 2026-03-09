package com.di2it.auth_service.service;

/**
 * Thrown when registering a user with an email that is already in use.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
