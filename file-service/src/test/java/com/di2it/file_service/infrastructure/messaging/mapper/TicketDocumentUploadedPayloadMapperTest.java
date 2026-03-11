package com.di2it.file_service.infrastructure.messaging.mapper;

import com.di2it.file_service.application.event.TicketDocumentUploadedEvent;
import com.di2it.file_service.infrastructure.messaging.payload.TicketDocumentUploadedPayload;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TicketDocumentUploadedPayloadMapperTest {

    private final TicketDocumentUploadedPayloadMapper mapper = new TicketDocumentUploadedPayloadMapper();

    @Test
    @DisplayName("maps event to payload with all fields")
    void toPayload_mapsAllFields() {
        UUID attachmentId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID uploadedBy = UUID.randomUUID();
        Instant timestamp = Instant.now();
        TicketDocumentUploadedEvent event = TicketDocumentUploadedEvent.builder()
            .attachmentId(attachmentId)
            .ticketId(ticketId)
            .tenantId(tenantId)
            .uploadedBy(uploadedBy)
            .fileName("doc.pdf")
            .mimeType("application/pdf")
            .fileSize(1024L)
            .timestamp(timestamp)
            .build();

        TicketDocumentUploadedPayload payload = mapper.toPayload(event);

        assertThat(payload).isNotNull();
        assertThat(payload.eventType()).isEqualTo(TicketDocumentUploadedPayload.EVENT_TYPE);
        assertThat(payload.eventVersion()).isEqualTo(TicketDocumentUploadedPayload.EVENT_VERSION);
        assertThat(payload.attachmentId()).isEqualTo(attachmentId);
        assertThat(payload.ticketId()).isEqualTo(ticketId);
        assertThat(payload.tenantId()).isEqualTo(tenantId);
        assertThat(payload.uploadedBy()).isEqualTo(uploadedBy);
        assertThat(payload.fileName()).isEqualTo("doc.pdf");
        assertThat(payload.mimeType()).isEqualTo("application/pdf");
        assertThat(payload.fileSize()).isEqualTo(1024L);
        assertThat(payload.timestamp()).isEqualTo(timestamp);
    }

    @Test
    @DisplayName("returns null when event is null")
    void toPayload_returnsNullWhenEventNull() {
        assertThat(mapper.toPayload(null)).isNull();
    }
}
