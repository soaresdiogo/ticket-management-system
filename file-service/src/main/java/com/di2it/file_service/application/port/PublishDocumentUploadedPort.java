package com.di2it.file_service.application.port;

import com.di2it.file_service.application.event.TicketDocumentUploadedEvent;

/**
 * Port for publishing ticket document uploaded events (e.g. to Kafka).
 */
@FunctionalInterface
public interface PublishDocumentUploadedPort {

    /**
     * Publishes the document uploaded event to the messaging infrastructure.
     *
     * @param event the event payload (attachmentId, ticketId, tenantId, uploadedBy, fileName, etc.)
     */
    void publish(TicketDocumentUploadedEvent event);
}
