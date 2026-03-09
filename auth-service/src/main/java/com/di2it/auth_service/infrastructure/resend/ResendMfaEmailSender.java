package com.di2it.auth_service.infrastructure.resend;

import com.di2it.auth_service.application.port.MfaEmailSender;
import com.di2it.auth_service.service.EmailDeliveryException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Sends MFA code emails via Resend API.
 *
 * @see <a href="https://resend.com/docs/api-reference/emails/send-email">Resend Send Email API</a>
 */
@Component
public class ResendMfaEmailSender implements MfaEmailSender {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final RestClient restClient;
    private final String fromEmail;

    public ResendMfaEmailSender(
        @Value("${resend.api-key:}") String apiKey,
        @Value("${resend.from-email:TMS Auth <noreply@example.com>}") String fromEmail
    ) {
        this.fromEmail = fromEmail;
        this.restClient = RestClient.builder()
            .baseUrl(RESEND_API_URL)
            .defaultHeader("Authorization", "Bearer " + (apiKey != null ? apiKey : ""))
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Override
    public void sendMfaCode(String to, String code) {
        String subject = "Your login verification code";
        String html = buildMfaEmailHtml(code);

        Map<String, Object> body = Map.of(
            "from", fromEmail,
            "to", List.of(to),
            "subject", subject,
            "html", html
        );

        try {
            restClient.post()
                .body(body)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            throw new EmailDeliveryException("Failed to send MFA email", e);
        }
    }

    private static String buildMfaEmailHtml(String code) {
        return """
            <p>Your verification code is: <strong>%s</strong></p>
            <p>This code expires in 5 minutes. Do not share it with anyone.</p>
            """.formatted(code);
    }
}
