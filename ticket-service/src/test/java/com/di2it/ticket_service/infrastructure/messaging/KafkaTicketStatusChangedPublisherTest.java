package com.di2it.ticket_service.infrastructure.messaging;

import com.di2it.ticket_service.application.event.TicketStatusChangedEvent;
import com.di2it.ticket_service.infrastructure.messaging.mapper.TicketStatusChangedPayloadMapper;
import com.di2it.ticket_service.infrastructure.messaging.payload.TicketStatusChangedPayload;

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
    private KafkaTemplate<String, TicketStatusChangedPayload> kafkaTemplate;

    @Mock
    private TicketStatusChangedPayloadMapper payloadMapper;

    private KafkaTicketStatusChangedPublisher publisher;

    private TicketStatusChangedEvent event;

    @BeforeEach
    void setUp() {
        publisher = new KafkaTicketStatusChangedPublisher(kafkaTemplate, payloadMapper, TOPIC);
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
    @DisplayName("publish maps event to payload and sends to Kafka with ticketId as key")
    void publishSendsPayloadToKafka() {
        TicketStatusChangedPayload payload = new TicketStatusChangedPayload(
            TicketStatusChangedPayload.EVENT_TYPE,
            TicketStatusChangedPayload.EVENT_VERSION,
            event.getTicketId(),
            event.getUserId(),
            "OPEN",
            "IN_PROGRESS",
            event.getTimestamp()
        );
        org.mockito.Mockito.when(payloadMapper.toPayload(event)).thenReturn(payload);

        publisher.publish(event);

        ArgumentCaptor<TicketStatusChangedPayload> captor = ArgumentCaptor.forClass(TicketStatusChangedPayload.class);
        verify(payloadMapper).toPayload(event);
        verify(kafkaTemplate).send(eq(TOPIC), eq(event.getTicketId().toString()), captor.capture());
        TicketStatusChangedPayload sent = captor.getValue();
        assertThat(sent.eventType()).isEqualTo(TicketStatusChangedPayload.EVENT_TYPE);
        assertThat(sent.eventVersion()).isEqualTo(TicketStatusChangedPayload.EVENT_VERSION);
        assertThat(sent.ticketId()).isEqualTo(event.getTicketId());
        assertThat(sent.userId()).isEqualTo(event.getUserId());
        assertThat(sent.oldStatus()).isEqualTo("OPEN");
        assertThat(sent.newStatus()).isEqualTo("IN_PROGRESS");
        assertThat(sent.timestamp()).isEqualTo(event.getTimestamp());
    }

    @Test
    @DisplayName("publish does not send when mapper returns null")
    void publishDoesNotSendWhenPayloadIsNull() {
        org.mockito.Mockito.when(payloadMapper.toPayload(event)).thenReturn(null);

        publisher.publish(event);

        verify(payloadMapper).toPayload(event);
        verify(kafkaTemplate, org.mockito.Mockito.never()).send(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any()
        );
    }
}
