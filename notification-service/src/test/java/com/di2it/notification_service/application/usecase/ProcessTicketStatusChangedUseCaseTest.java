package com.di2it.notification_service.application.usecase;

import com.di2it.notification_service.application.port.PersistEmailLogPort;
import com.di2it.notification_service.application.port.PersistNotificationPort;
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
class ProcessTicketStatusChangedUseCaseTest {

    @Mock
    private PersistNotificationPort persistNotificationPort;

    @Mock
    private PersistEmailLogPort persistEmailLogPort;

    @Mock
    private ResolveUserEmailPort resolveUserEmailPort;

    @Mock
    private SendNotificationEmailPort sendNotificationEmailPort;

    @InjectMocks
    private ProcessTicketStatusChangedUseCase useCase;

    private UUID ticketId;
    private UUID tenantId;
    private UUID clientId;
    private Notification savedNotification;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        savedNotification = Notification.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .userId(clientId)
            .type("TICKET_STATUS_CHANGED")
            .title("Ticket updated")
            .message("Updated to Processing")
            .referenceId(ticketId)
            .read(false)
            .build();
    }

    @Test
    @DisplayName("persists notification and sends email when client email is resolved")
    void process_persistsAndSendsEmailWhenEmailResolved() {
        when(persistNotificationPort.save(any(Notification.class))).thenReturn(savedNotification);
        when(resolveUserEmailPort.resolveEmail(clientId)).thenReturn(Optional.of("client@example.com"));
        when(sendNotificationEmailPort.send(any(), any(), any()))
            .thenReturn(SendNotificationEmailPort.SendResult.ok("resend-123"));

        useCase.process(ticketId, tenantId, clientId, "Processing");

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(persistNotificationPort).save(notificationCaptor.capture());
        Notification captured = notificationCaptor.getValue();
        assertThat(captured.getTenantId()).isEqualTo(tenantId);
        assertThat(captured.getUserId()).isEqualTo(clientId);
        assertThat(captured.getType()).isEqualTo("TICKET_STATUS_CHANGED");
        assertThat(captured.getReferenceId()).isEqualTo(ticketId);
        assertThat(captured.getMessage()).contains("Processing");

        verify(sendNotificationEmailPort).send("client@example.com", captured.getTitle(), any());
        verify(persistEmailLogPort).save(any());
    }

    @Test
    @DisplayName("persists notification but does not send email when client email not resolved")
    void process_doesNotSendEmailWhenEmailNotResolved() {
        when(persistNotificationPort.save(any(Notification.class))).thenReturn(savedNotification);
        when(resolveUserEmailPort.resolveEmail(clientId)).thenReturn(Optional.empty());

        useCase.process(ticketId, tenantId, clientId, "Resolved");

        verify(persistNotificationPort).save(any());
        verify(sendNotificationEmailPort, never()).send(any(), any(), any());
        verify(persistEmailLogPort, never()).save(any());
    }
}
