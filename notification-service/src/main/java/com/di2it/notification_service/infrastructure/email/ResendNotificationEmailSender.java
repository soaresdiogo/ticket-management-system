package com.di2it.notification_service.infrastructure.email;

import com.di2it.notification_service.application.port.SendNotificationEmailPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Sends notification emails via Resend API.
 *
 * @see <a href="https://resend.com/docs/api-reference/emails/send-email">Resend Send Email API</a>
 */
@Component
public class ResendNotificationEmailSender implements SendNotificationEmailPort {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final RestClient restClient;
    private final String fromEmail;

    public ResendNotificationEmailSender(
        @Value("${resend.api-key:}") String apiKey,
        @Value("${resend.from-email:TMS Notifications <noreply@example.com>}") String fromEmail
    ) {
        this.fromEmail = fromEmail;
        this.restClient = RestClient.builder()
            .baseUrl(RESEND_API_URL)
            .defaultHeader("Authorization", "Bearer " + (apiKey != null ? apiKey : ""))
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Override
    public SendResult send(String to, String subject, String htmlBody) {
        Map<String, Object> body = Map.of(
            "from", fromEmail,
            "to", List.of(to),
            "subject", subject,
            "html", htmlBody
        );
        try {
            ResendResponse response = restClient.post()
                .body(body)
                .retrieve()
                .body(ResendResponse.class);
            String id = response != null ? response.id() : null;
            return SendResult.ok(id != null ? id : "");
        } catch (Exception e) {
            return SendResult.failure(e.getMessage());
        }
    }

    private record ResendResponse(String id) {
    }
}
