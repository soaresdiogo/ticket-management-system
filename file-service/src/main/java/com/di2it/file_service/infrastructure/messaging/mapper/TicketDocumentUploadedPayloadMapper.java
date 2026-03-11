package com.di2it.file_service.infrastructure.messaging.mapper;

import com.di2it.file_service.application.event.TicketDocumentUploadedEvent;
import com.di2it.file_service.infrastructure.messaging.payload.TicketDocumentUploadedPayload;

import org.springframework.stereotype.Component;

/**
 * Maps domain event to Kafka wire payload. Keeps serialization contract in infrastructure.
 */
@Component
public class TicketDocumentUploadedPayloadMapper {

    public TicketDocumentUploadedPayload toPayload(TicketDocumentUploadedEvent event) {
        if (event == null) {
            return null;
        }
        return new TicketDocumentUploadedPayload(
            TicketDocumentUploadedPayload.EVENT_TYPE,
            TicketDocumentUploadedPayload.EVENT_VERSION,
            event.getAttachmentId(),
            event.getTicketId(),
            event.getTenantId(),
            event.getUploadedBy(),
            event.getFileName(),
            event.getMimeType(),
            event.getFileSize(),
            event.getTimestamp()
        );
    }
}
