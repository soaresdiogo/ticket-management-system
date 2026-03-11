package com.di2it.file_service.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event payload for document upload. Published to Kafka topic ticket.document.uploaded.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDocumentUploadedEvent {

    private UUID attachmentId;
    private UUID ticketId;
    private UUID tenantId;
    private UUID uploadedBy;
    private String fileName;
    private String mimeType;
    private long fileSize;
    private Instant timestamp;
}
