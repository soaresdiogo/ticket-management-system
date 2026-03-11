package com.di2it.notification_service.application.usecase;

import com.di2it.notification_service.application.port.PersistEmailLogPort;
import com.di2it.notification_service.application.port.PersistNotificationPort;
import com.di2it.notification_service.application.port.ResolveUserEmailPort;
import com.di2it.notification_service.application.port.SendNotificationEmailPort;
import com.di2it.notification_service.domain.entity.EmailLog;
import com.di2it.notification_service.domain.entity.Notification;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Use case: process ticket.document.uploaded Kafka event — persist notification, send email via Resend.
 */
@Service
public class ProcessDocumentUploadedUseCase {

    private static final String NOTIFICATION_TYPE = "TICKET_DOCUMENT_UPLOADED";

    private final PersistNotificationPort persistNotificationPort;
    private final PersistEmailLogPort persistEmailLogPort;
    private final ResolveUserEmailPort resolveUserEmailPort;
    private final SendNotificationEmailPort sendNotificationEmailPort;

    public ProcessDocumentUploadedUseCase(
        PersistNotificationPort persistNotificationPort,
        PersistEmailLogPort persistEmailLogPort,
        ResolveUserEmailPort resolveUserEmailPort,
        SendNotificationEmailPort sendNotificationEmailPort
    ) {
        this.persistNotificationPort = persistNotificationPort;
        this.persistEmailLogPort = persistEmailLogPort;
        this.resolveUserEmailPort = resolveUserEmailPort;
        this.sendNotificationEmailPort = sendNotificationEmailPort;
    }

    /**
     * Persists a notification for the uploader and sends email if recipient email is resolved.
     *
     * @param ticketId   ticket id
     * @param tenantId   tenant id
     * @param uploadedBy user who uploaded (to notify)
     * @param fileName   uploaded file name
     */
    public void process(UUID ticketId, UUID tenantId, UUID uploadedBy, String fileName) {
        String title = "Document attached to ticket #" + ticketId.toString().substring(0, 8);
        String message = "Your document \"" + fileName + "\" was attached to ticket #" + ticketId + ".";

        Notification notification = Notification.builder()
            .tenantId(tenantId)
            .userId(uploadedBy)
            .type(NOTIFICATION_TYPE)
            .title(title)
            .message(message)
            .referenceId(ticketId)
            .read(false)
            .createdAt(Instant.now())
            .build();

        Notification saved = persistNotificationPort.save(notification);

        resolveUserEmailPort.resolveEmail(uploadedBy).ifPresent(email -> {
            String subject = title;
            String htmlBody = buildDocumentUploadedEmailHtml(ticketId, fileName);
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

    private static String buildDocumentUploadedEmailHtml(UUID ticketId, String fileName) {
        return "<p>Your document <strong>\"" + fileName + "\"</strong> was attached to ticket <strong>#"
            + ticketId + "</strong>.</p>";
    }
}
