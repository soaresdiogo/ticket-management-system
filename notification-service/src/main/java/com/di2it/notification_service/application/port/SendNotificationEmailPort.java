package com.di2it.notification_service.application.port;

/**
 * Port for sending notification emails (e.g. via Resend). Not a single abstract method (has nested record).
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface SendNotificationEmailPort {

    /**
     * Sends an email to the given recipient.
     *
     * @param to      recipient email
     * @param subject subject line
     * @param htmlBody HTML body
     * @return Resend message id if successful, or empty on failure (caller may log)
     */
    SendResult send(String to, String subject, String htmlBody);

    /**
     * Result of a send attempt.
     */
    record SendResult(boolean success, String resendId, String errorMessage) {
        public static SendResult ok(String resendId) {
            return new SendResult(true, resendId, null);
        }

        public static SendResult failure(String errorMessage) {
            return new SendResult(false, null, errorMessage);
        }
    }
}
