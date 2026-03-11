package com.di2it.notification_service.infrastructure.messaging.payload;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.UUID;

/**
 * Wire contract for ticket document uploaded events consumed from Kafka topic
 * {@code ticket.document.uploaded}.
 */
public record TicketDocumentUploadedPayload(
    String eventType,
    String eventVersion,
    UUID attachmentId,
    UUID ticketId,
    UUID tenantId,
    UUID uploadedBy,
    String fileName,
    String mimeType,
    long fileSize,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant timestamp
) {
}
