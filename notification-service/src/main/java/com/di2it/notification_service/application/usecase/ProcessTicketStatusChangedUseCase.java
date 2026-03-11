package com.di2it.notification_service.application.usecase;

import com.di2it.notification_service.application.port.PersistEmailLogPort;
import com.di2it.notification_service.application.port.PersistNotificationPort;
import com.di2it.notification_service.application.port.PushNotificationPort;
import com.di2it.notification_service.application.port.ResolveUserEmailPort;
import com.di2it.notification_service.application.port.SendNotificationEmailPort;
import com.di2it.notification_service.domain.entity.EmailLog;
import com.di2it.notification_service.domain.entity.Notification;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Use case: process ticket.status.changed Kafka event — persist notification, send email via Resend.
 */
@Service
public class ProcessTicketStatusChangedUseCase {

    private static final String NOTIFICATION_TYPE = "TICKET_STATUS_CHANGED";

    private final PersistNotificationPort persistNotificationPort;
    private final PersistEmailLogPort persistEmailLogPort;
    private final ResolveUserEmailPort resolveUserEmailPort;
    private final SendNotificationEmailPort sendNotificationEmailPort;
    private final PushNotificationPort pushNotificationPort;

    public ProcessTicketStatusChangedUseCase(
        PersistNotificationPort persistNotificationPort,
        PersistEmailLogPort persistEmailLogPort,
        ResolveUserEmailPort resolveUserEmailPort,
        SendNotificationEmailPort sendNotificationEmailPort,
        PushNotificationPort pushNotificationPort
    ) {
        this.persistNotificationPort = persistNotificationPort;
        this.persistEmailLogPort = persistEmailLogPort;
        this.resolveUserEmailPort = resolveUserEmailPort;
        this.sendNotificationEmailPort = sendNotificationEmailPort;
        this.pushNotificationPort = pushNotificationPort;
    }

    /**
     * Persists a notification for the ticket client and sends email if recipient email is resolved.
     *
     * @param ticketId  ticket id
     * @param tenantId  tenant id
     * @param clientId  user to notify (ticket owner)
     * @param newStatus new ticket status
     */
    public void process(UUID ticketId, UUID tenantId, UUID clientId, String newStatus) {
        String title = "Ticket #" + ticketId.toString().substring(0, 8) + " status updated";
        String message = "Your ticket #" + ticketId + " has been updated to " + newStatus + ".";

        Notification notification = Notification.builder()
            .tenantId(tenantId)
            .userId(clientId)
            .type(NOTIFICATION_TYPE)
            .title(title)
            .message(message)
            .referenceId(ticketId)
            .read(false)
            .createdAt(Instant.now())
            .build();

        Notification saved = persistNotificationPort.save(notification);
        pushNotificationPort.push(saved);

        resolveUserEmailPort.resolveEmail(clientId).ifPresent(email -> {
            String subject = title;
            String htmlBody = buildStatusChangedEmailHtml(ticketId, newStatus);
            SendNotificationEmailPort.SendResult result = sendNotificationEmailPort.send(email, subject, htmlBody);

            EmailLog log = EmailLog.builder()
                .notification(saved)
                .recipientEmail(email)
                .subject(subject)
                .status(result.success() ? "SENT" : "FAILED")
                .resendId(result.resendId())
                .errorMessage(result.errorMessage())
                .createdAt(Instant.now())
                .build();
            persistEmailLogPort.save(log);
        });
    }

    private static String buildStatusChangedEmailHtml(UUID ticketId, String newStatus) {
        return "<p>Your ticket <strong>#" + ticketId + "</strong> has been updated to <strong>"
            + newStatus + "</strong>.</p>";
    }
}
