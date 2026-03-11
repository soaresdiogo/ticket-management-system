package com.di2it.file_service.infrastructure.messaging;

import com.di2it.file_service.application.event.TicketDocumentUploadedEvent;
import com.di2it.file_service.application.port.PublishDocumentUploadedPort;
import com.di2it.file_service.infrastructure.messaging.mapper.TicketDocumentUploadedPayloadMapper;
import com.di2it.file_service.infrastructure.messaging.payload.TicketDocumentUploadedPayload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter that publishes document uploaded events to the configured topic.
 * Maps domain events to the wire payload (eventType, eventVersion, attachmentId, ticketId, etc.).
 */
@Component
public class KafkaDocumentUploadedPublisher implements PublishDocumentUploadedPort {

    public static final String DEFAULT_TOPIC = "ticket.document.uploaded";

    private final KafkaTemplate<String, TicketDocumentUploadedPayload> kafkaTemplate;
    private final TicketDocumentUploadedPayloadMapper payloadMapper;
    private final String topic;

    public KafkaDocumentUploadedPublisher(
        KafkaTemplate<String, TicketDocumentUploadedPayload> documentUploadedKafkaTemplate,
        TicketDocumentUploadedPayloadMapper payloadMapper,
        @Value("${file-service.kafka.topic.document-uploaded:" + DEFAULT_TOPIC + "}") String topic
    ) {
        this.kafkaTemplate = documentUploadedKafkaTemplate;
        this.payloadMapper = payloadMapper;
        this.topic = topic;
    }

    @Override
    public void publish(TicketDocumentUploadedEvent event) {
        TicketDocumentUploadedPayload payload = payloadMapper.toPayload(event);
        if (payload != null) {
            kafkaTemplate.send(topic, event.getAttachmentId().toString(), payload);
        }
    }
}
