package com.di2it.ticket_service.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event payload for ticket status change. Published to Kafka topic ticket.status.changed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusChangedEvent {

    private UUID ticketId;
    private UUID tenantId;
    private UUID userId;
    private UUID clientId;
    private String oldStatus;
    private String newStatus;
    private Instant timestamp;
}
