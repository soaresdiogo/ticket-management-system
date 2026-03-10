package com.di2it.ticket_service.infrastructure.messaging;

import com.di2it.ticket_service.application.event.TicketStatusChangedEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaTicketStatusChangedPublisherTest {

    private static final String TOPIC = "ticket.status.changed";

    @Mock
    private KafkaTemplate<String, TicketStatusChangedEvent> kafkaTemplate;

    private KafkaTicketStatusChangedPublisher publisher;

    private TicketStatusChangedEvent event;

    @BeforeEach
    void setUp() {
        publisher = new KafkaTicketStatusChangedPublisher(kafkaTemplate, TOPIC);
        UUID ticketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        event = TicketStatusChangedEvent.builder()
            .ticketId(ticketId)
            .userId(userId)
            .oldStatus("OPEN")
            .newStatus("IN_PROGRESS")
            .timestamp(Instant.now())
            .build();
    }

    @Test
    @DisplayName("publish sends event to Kafka with ticketId as key")
    void publishSendsToKafka() {
        publisher.publish(event);

        ArgumentCaptor<TicketStatusChangedEvent> captor = ArgumentCaptor.forClass(TicketStatusChangedEvent.class);
        verify(kafkaTemplate).send(eq(TOPIC), eq(event.getTicketId().toString()), captor.capture());
        TicketStatusChangedEvent sent = captor.getValue();
        assertThat(sent.getTicketId()).isEqualTo(event.getTicketId());
        assertThat(sent.getUserId()).isEqualTo(event.getUserId());
        assertThat(sent.getOldStatus()).isEqualTo("OPEN");
        assertThat(sent.getNewStatus()).isEqualTo("IN_PROGRESS");
        assertThat(sent.getTimestamp()).isEqualTo(event.getTimestamp());
    }
}
