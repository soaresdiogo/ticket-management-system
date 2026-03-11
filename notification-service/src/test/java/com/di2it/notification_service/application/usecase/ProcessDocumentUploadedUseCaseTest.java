package com.di2it.notification_service.application.usecase;

import com.di2it.notification_service.application.port.PersistEmailLogPort;
import com.di2it.notification_service.application.port.PersistNotificationPort;
import com.di2it.notification_service.application.port.PushNotificationPort;
import com.di2it.notification_service.application.port.ResolveUserEmailPort;
import com.di2it.notification_service.application.port.SendNotificationEmailPort;
import com.di2it.notification_service.domain.entity.Notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessDocumentUploadedUseCaseTest {

    @Mock
    private PersistNotificationPort persistNotificationPort;

    @Mock
    private PersistEmailLogPort persistEmailLogPort;

    @Mock
    private ResolveUserEmailPort resolveUserEmailPort;

    @Mock
    private SendNotificationEmailPort sendNotificationEmailPort;

    @Mock
    private PushNotificationPort pushNotificationPort;

    @InjectMocks
    private ProcessDocumentUploadedUseCase useCase;

    private UUID ticketId;
    private UUID tenantId;
    private UUID uploadedBy;
    private Notification savedNotification;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        uploadedBy = UUID.randomUUID();
        savedNotification = Notification.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .userId(uploadedBy)
            .type("TICKET_DOCUMENT_UPLOADED")
            .title("Document attached")
            .message("file.pdf attached")
            .referenceId(ticketId)
            .read(false)
            .build();
    }

    @Test
    @DisplayName("persists notification and sends email when uploader email is resolved")
    void process_persistsAndSendsEmailWhenEmailResolved() {
        when(persistNotificationPort.save(any(Notification.class))).thenReturn(savedNotification);
        when(resolveUserEmailPort.resolveEmail(uploadedBy)).thenReturn(Optional.of("user@example.com"));
        when(sendNotificationEmailPort.send(any(), any(), any()))
            .thenReturn(SendNotificationEmailPort.SendResult.ok("resend-456"));

        useCase.process(ticketId, tenantId, uploadedBy, "invoice.pdf");

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(persistNotificationPort).save(notificationCaptor.capture());
        Notification captured = notificationCaptor.getValue();
        assertThat(captured.getUserId()).isEqualTo(uploadedBy);
        assertThat(captured.getType()).isEqualTo("TICKET_DOCUMENT_UPLOADED");
        assertThat(captured.getMessage()).contains("invoice.pdf");
        assertThat(captured.getReferenceId()).isEqualTo(ticketId);

        verify(sendNotificationEmailPort).send("user@example.com", captured.getTitle(), any());
        verify(persistEmailLogPort).save(any());
    }

    @Test
    @DisplayName("persists notification but does not send email when email not resolved")
    void process_doesNotSendEmailWhenEmailNotResolved() {
        when(persistNotificationPort.save(any(Notification.class))).thenReturn(savedNotification);
        when(resolveUserEmailPort.resolveEmail(uploadedBy)).thenReturn(Optional.empty());

        useCase.process(ticketId, tenantId, uploadedBy, "doc.pdf");

        verify(persistNotificationPort).save(any());
        verify(sendNotificationEmailPort, never()).send(any(), any(), any());
        verify(persistEmailLogPort, never()).save(any());
    }
}
