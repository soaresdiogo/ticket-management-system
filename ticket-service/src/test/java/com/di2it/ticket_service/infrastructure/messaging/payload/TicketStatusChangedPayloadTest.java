package com.di2it.ticket_service.infrastructure.messaging.payload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TicketStatusChangedPayloadTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("serializes to JSON with ISO-8601 timestamp")
    void serializesToJson() throws Exception {
        UUID ticketId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID tenantId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID clientId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        Instant timestamp = Instant.parse("2025-03-10T12:00:00.000Z");
        TicketStatusChangedPayload payload = new TicketStatusChangedPayload(
            TicketStatusChangedPayload.EVENT_TYPE,
            TicketStatusChangedPayload.EVENT_VERSION,
            ticketId,
            tenantId,
            userId,
            clientId,
            "OPEN",
            "IN_PROGRESS",
            timestamp
        );

        String json = objectMapper.writeValueAsString(payload);

        assertThat(json).contains("\"eventType\":\"TicketStatusChanged\"");
        assertThat(json).contains("\"eventVersion\":\"1.0\"");
        assertThat(json).contains("\"ticketId\":\"11111111-1111-1111-1111-111111111111\"");
        assertThat(json).contains("\"tenantId\":\"33333333-3333-3333-3333-333333333333\"");
        assertThat(json).contains("\"userId\":\"22222222-2222-2222-2222-222222222222\"");
        assertThat(json).contains("\"clientId\":\"44444444-4444-4444-4444-444444444444\"");
        assertThat(json).contains("\"oldStatus\":\"OPEN\"");
        assertThat(json).contains("\"newStatus\":\"IN_PROGRESS\"");
        assertThat(json).contains("2025-03-10T12:00:00.000Z");
    }

    @Test
    @DisplayName("deserializes from JSON back to payload")
    void deserializesFromJson() throws Exception {
        String json = """
            {"eventType":"TicketStatusChanged","eventVersion":"1.0",\
            "ticketId":"11111111-1111-1111-1111-111111111111",\
            "tenantId":"33333333-3333-3333-3333-333333333333",\
            "userId":"22222222-2222-2222-2222-222222222222",\
            "clientId":"44444444-4444-4444-4444-444444444444",\
            "oldStatus":"OPEN","newStatus":"IN_PROGRESS",\
            "timestamp":"2025-03-10T12:00:00.000Z"}
            """;

        TicketStatusChangedPayload payload = objectMapper.readValue(json, TicketStatusChangedPayload.class);

        assertThat(payload)
            .extracting(
                TicketStatusChangedPayload::eventType,
                TicketStatusChangedPayload::eventVersion,
                p -> p.ticketId().toString(),
                p -> p.tenantId().toString(),
                p -> p.userId().toString(),
                p -> p.clientId().toString(),
                TicketStatusChangedPayload::oldStatus,
                TicketStatusChangedPayload::newStatus,
                TicketStatusChangedPayload::timestamp
            )
            .containsExactly(
                TicketStatusChangedPayload.EVENT_TYPE,
                TicketStatusChangedPayload.EVENT_VERSION,
                "11111111-1111-1111-1111-111111111111",
                "33333333-3333-3333-3333-333333333333",
                "22222222-2222-2222-2222-222222222222",
                "44444444-4444-4444-4444-444444444444",
                "OPEN",
                "IN_PROGRESS",
                Instant.parse("2025-03-10T12:00:00.000Z")
            );
    }
}
