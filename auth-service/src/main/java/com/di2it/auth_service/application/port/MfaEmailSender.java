package com.di2it.auth_service.application.port;

/**
 * Port for sending MFA code by email (e.g. via Resend).
 */
@FunctionalInterface
public interface MfaEmailSender {

    /**
     * Sends the MFA code to the given email address.
     *
     * @param to   recipient email
     * @param code the MFA code to send
     * @throws com.di2it.auth_service.service.EmailDeliveryException if sending fails
     */
    void sendMfaCode(String to, String code);
}
