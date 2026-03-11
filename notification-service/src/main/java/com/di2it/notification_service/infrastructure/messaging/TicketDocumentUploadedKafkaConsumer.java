package com.di2it.notification_service.infrastructure.messaging;

import com.di2it.notification_service.application.usecase.ProcessDocumentUploadedUseCase;
import com.di2it.notification_service.infrastructure.messaging.payload.TicketDocumentUploadedPayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for ticket.document.uploaded. Persists notification and sends email via use case.
 */
@Component
@ConditionalOnProperty(name = "notification-service.kafka.enabled", havingValue = "true")
public class TicketDocumentUploadedKafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TicketDocumentUploadedKafkaConsumer.class);

    private final ProcessDocumentUploadedUseCase processDocumentUploadedUseCase;

    public TicketDocumentUploadedKafkaConsumer(ProcessDocumentUploadedUseCase processDocumentUploadedUseCase) {
        this.processDocumentUploadedUseCase = processDocumentUploadedUseCase;
    }

    @KafkaListener(
        topics = "${notification-service.kafka.topic.document-uploaded:ticket.document.uploaded}",
        groupId = "${spring.kafka.consumer.group-id:notification-service}",
        containerFactory = "documentUploadedListenerContainerFactory"
    )
    public void consume(TicketDocumentUploadedPayload payload) {
        if (payload == null || payload.ticketId() == null || payload.uploadedBy() == null) {
            LOG.warn("Ignoring invalid ticket.document.uploaded payload");
            return;
        }
        try {
            processDocumentUploadedUseCase.process(
                payload.ticketId(),
                payload.tenantId(),
                payload.uploadedBy(),
                payload.fileName() != null ? payload.fileName() : "document"
            );
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Failed to process ticket.document.uploaded for ticket {}", payload.ticketId(), e);
            }
        }
    }
}
