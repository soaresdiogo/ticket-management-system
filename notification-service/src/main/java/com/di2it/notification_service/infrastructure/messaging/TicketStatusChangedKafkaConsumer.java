package com.di2it.notification_service.infrastructure.messaging;

import com.di2it.notification_service.application.usecase.ProcessTicketStatusChangedUseCase;
import com.di2it.notification_service.infrastructure.messaging.payload.TicketStatusChangedPayload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for ticket.status.changed. Persists notification and sends email via use case.
 */
@Component
@ConditionalOnProperty(name = "notification-service.kafka.enabled", havingValue = "true")
public class TicketStatusChangedKafkaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TicketStatusChangedKafkaConsumer.class);

    private final ProcessTicketStatusChangedUseCase processTicketStatusChangedUseCase;

    public TicketStatusChangedKafkaConsumer(ProcessTicketStatusChangedUseCase processTicketStatusChangedUseCase) {
        this.processTicketStatusChangedUseCase = processTicketStatusChangedUseCase;
    }

    @KafkaListener(
        topics = "${notification-service.kafka.topic.status-changed:ticket.status.changed}",
        groupId = "${spring.kafka.consumer.group-id:notification-service}",
        containerFactory = "statusChangedListenerContainerFactory"
    )
    public void consume(TicketStatusChangedPayload payload) {
        if (payload == null || payload.ticketId() == null || payload.clientId() == null) {
            LOG.warn("Ignoring invalid ticket.status.changed payload");
            return;
        }
        try {
            processTicketStatusChangedUseCase.process(
                payload.ticketId(),
                payload.tenantId(),
                payload.clientId(),
                payload.newStatus()
            );
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Failed to process ticket.status.changed for ticket {}", payload.ticketId(), e);
            }
        }
    }
}
