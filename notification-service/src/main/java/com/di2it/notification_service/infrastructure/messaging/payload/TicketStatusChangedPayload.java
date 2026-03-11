package com.di2it.notification_service.infrastructure.messaging.payload;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.UUID;

/**
 * Wire contract for ticket status changed events consumed from Kafka topic {@code ticket.status.changed}.
 */
public record TicketStatusChangedPayload(
    String eventType,
    String eventVersion,
    UUID ticketId,
    UUID tenantId,
    UUID userId,
    UUID clientId,
    String oldStatus,
    String newStatus,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant timestamp
) {
}
