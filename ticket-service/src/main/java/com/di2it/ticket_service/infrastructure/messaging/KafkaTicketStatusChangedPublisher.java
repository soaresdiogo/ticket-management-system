package com.di2it.ticket_service.infrastructure.messaging;

import com.di2it.ticket_service.application.event.TicketStatusChangedEvent;
import com.di2it.ticket_service.application.port.PublishTicketStatusChangedPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter that publishes ticket status changed events to the configured topic.
 */
@Component
public class KafkaTicketStatusChangedPublisher implements PublishTicketStatusChangedPort {

    public static final String DEFAULT_TOPIC = "ticket.status.changed";

    private final KafkaTemplate<String, TicketStatusChangedEvent> kafkaTemplate;
    private final String topic;

    public KafkaTicketStatusChangedPublisher(
        KafkaTemplate<String, TicketStatusChangedEvent> ticketStatusChangedKafkaTemplate,
        @Value("${ticket-service.kafka.topic.status-changed:" + DEFAULT_TOPIC + "}") String topic
    ) {
        this.kafkaTemplate = ticketStatusChangedKafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(TicketStatusChangedEvent event) {
        kafkaTemplate.send(topic, event.getTicketId().toString(), event);
    }
}
