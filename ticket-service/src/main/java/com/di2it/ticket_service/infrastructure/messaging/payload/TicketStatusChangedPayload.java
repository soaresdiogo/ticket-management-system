package com.di2it.ticket_service.infrastructure.messaging.payload;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.UUID;

/**
 * Wire contract for ticket status changed events published to Kafka topic {@code ticket.status.changed}.
 * Consumers can use {@code eventType} and {@code eventVersion} for routing and evolution.
 */
public record TicketStatusChangedPayload(
    String eventType,
    String eventVersion,
    UUID ticketId,
    UUID userId,
    String oldStatus,
    String newStatus,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Instant timestamp
) {
    public static final String EVENT_TYPE = "TicketStatusChanged";
    public static final String EVENT_VERSION = "1.0";
}
