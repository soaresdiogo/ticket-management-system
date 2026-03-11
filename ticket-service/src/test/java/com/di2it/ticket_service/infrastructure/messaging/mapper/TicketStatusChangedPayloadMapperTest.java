package com.di2it.ticket_service.infrastructure.messaging.mapper;

import com.di2it.ticket_service.application.event.TicketStatusChangedEvent;
import com.di2it.ticket_service.infrastructure.messaging.payload.TicketStatusChangedPayload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TicketStatusChangedPayloadMapperTest {

    private TicketStatusChangedPayloadMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TicketStatusChangedPayloadMapper();
    }

    @Test
    @DisplayName("toPayload maps event to payload with eventType and eventVersion")
    void toPayload_mapsEventToPayload() {
        UUID ticketId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        Instant timestamp = Instant.parse("2025-03-10T12:00:00.000Z");
        TicketStatusChangedEvent event = TicketStatusChangedEvent.builder()
            .ticketId(ticketId)
            .tenantId(tenantId)
            .userId(userId)
            .clientId(clientId)
            .oldStatus("OPEN")
            .newStatus("IN_PROGRESS")
            .timestamp(timestamp)
            .build();

        TicketStatusChangedPayload payload = mapper.toPayload(event);

        assertThat(payload).isNotNull();
        assertThat(payload.eventType()).isEqualTo(TicketStatusChangedPayload.EVENT_TYPE);
        assertThat(payload.eventVersion()).isEqualTo(TicketStatusChangedPayload.EVENT_VERSION);
        assertThat(payload.ticketId()).isEqualTo(ticketId);
        assertThat(payload.tenantId()).isEqualTo(tenantId);
        assertThat(payload.userId()).isEqualTo(userId);
        assertThat(payload.clientId()).isEqualTo(clientId);
        assertThat(payload.oldStatus()).isEqualTo("OPEN");
        assertThat(payload.newStatus()).isEqualTo("IN_PROGRESS");
        assertThat(payload.timestamp()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("toPayload returns null when event is null")
    void toPayload_returnsNullWhenEventIsNull() {
        TicketStatusChangedPayload payload = mapper.toPayload(null);
        assertThat(payload).isNull();
    }
}
