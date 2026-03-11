package com.di2it.file_service.infrastructure.messaging;

import com.di2it.file_service.application.event.TicketDocumentUploadedEvent;
import com.di2it.file_service.infrastructure.messaging.mapper.TicketDocumentUploadedPayloadMapper;
import com.di2it.file_service.infrastructure.messaging.payload.TicketDocumentUploadedPayload;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaDocumentUploadedPublisherTest {

    private static final String TOPIC = "ticket.document.uploaded";

    @Mock
    private KafkaTemplate<String, TicketDocumentUploadedPayload> kafkaTemplate;

    @Mock
    private TicketDocumentUploadedPayloadMapper payloadMapper;

    private KafkaDocumentUploadedPublisher publisher;

    private TicketDocumentUploadedEvent event;

    @BeforeEach
    void setUp() {
        publisher = new KafkaDocumentUploadedPublisher(kafkaTemplate, payloadMapper, TOPIC);
        UUID attachmentId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        event = TicketDocumentUploadedEvent.builder()
            .attachmentId(attachmentId)
            .ticketId(ticketId)
            .tenantId(UUID.randomUUID())
            .uploadedBy(UUID.randomUUID())
            .fileName("doc.pdf")
            .mimeType("application/pdf")
            .fileSize(100L)
            .timestamp(Instant.now())
            .build();
    }

    @Test
    @DisplayName("publish maps event to payload and sends to Kafka with attachmentId as key")
    void publishSendsPayloadToKafka() {
        TicketDocumentUploadedPayload payload = new TicketDocumentUploadedPayload(
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
        when(payloadMapper.toPayload(event)).thenReturn(payload);

        publisher.publish(event);

        ArgumentCaptor<TicketDocumentUploadedPayload> captor =
            ArgumentCaptor.forClass(TicketDocumentUploadedPayload.class);
        verify(payloadMapper).toPayload(event);
        verify(kafkaTemplate).send(eq(TOPIC), eq(event.getAttachmentId().toString()), captor.capture());
        TicketDocumentUploadedPayload sent = captor.getValue();
        assertThat(sent.eventType()).isEqualTo(TicketDocumentUploadedPayload.EVENT_TYPE);
        assertThat(sent.attachmentId()).isEqualTo(event.getAttachmentId());
        assertThat(sent.fileName()).isEqualTo("doc.pdf");
    }

    @Test
    @DisplayName("publish does not send when mapper returns null")
    void publishDoesNotSendWhenPayloadIsNull() {
        when(payloadMapper.toPayload(event)).thenReturn(null);

        publisher.publish(event);

        verify(payloadMapper).toPayload(event);
        verify(kafkaTemplate, never()).send(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any()
        );
    }
}
