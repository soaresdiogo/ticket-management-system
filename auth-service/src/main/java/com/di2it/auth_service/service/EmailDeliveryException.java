package com.di2it.auth_service.service;

/**
 * Thrown when MFA email delivery fails (e.g. Resend API error).
 */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message) {
        super(message);
    }

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
