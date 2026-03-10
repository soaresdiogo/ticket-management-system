package com.di2it.ticket_service.infrastructure.messaging;

import com.di2it.ticket_service.application.event.TicketStatusChangedEvent;
import com.di2it.ticket_service.application.port.PublishTicketStatusChangedPort;
import com.di2it.ticket_service.infrastructure.messaging.mapper.TicketStatusChangedPayloadMapper;
import com.di2it.ticket_service.infrastructure.messaging.payload.TicketStatusChangedPayload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter that publishes ticket status changed events to the configured topic.
 * Maps domain events to the wire payload (eventType, eventVersion, ticketId, userId, oldStatus, newStatus, timestamp).
 */
@Component
public class KafkaTicketStatusChangedPublisher implements PublishTicketStatusChangedPort {

    public static final String DEFAULT_TOPIC = "ticket.status.changed";

    private final KafkaTemplate<String, TicketStatusChangedPayload> kafkaTemplate;
    private final TicketStatusChangedPayloadMapper payloadMapper;
    private final String topic;

    public KafkaTicketStatusChangedPublisher(
        KafkaTemplate<String, TicketStatusChangedPayload> ticketStatusChangedKafkaTemplate,
        TicketStatusChangedPayloadMapper payloadMapper,
        @Value("${ticket-service.kafka.topic.status-changed:" + DEFAULT_TOPIC + "}") String topic
    ) {
        this.kafkaTemplate = ticketStatusChangedKafkaTemplate;
        this.payloadMapper = payloadMapper;
        this.topic = topic;
    }

    @Override
    public void publish(TicketStatusChangedEvent event) {
        TicketStatusChangedPayload payload = payloadMapper.toPayload(event);
        if (payload != null) {
            kafkaTemplate.send(topic, event.getTicketId().toString(), payload);
        }
    }
}
