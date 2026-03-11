package com.di2it.ticket_service.application.usecase;

import com.di2it.ticket_service.application.event.TicketStatusChangedEvent;
import com.di2it.ticket_service.application.port.FindTicketByIdAndTenantIdPort;
import com.di2it.ticket_service.application.port.PublishTicketStatusChangedPort;
import com.di2it.ticket_service.application.port.UpdateTicketStatusPort;
import com.di2it.ticket_service.domain.entity.Ticket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeTicketStatusUseCaseTest {

    @Mock
    private FindTicketByIdAndTenantIdPort findTicketPort;

    @Mock
    private UpdateTicketStatusPort updateTicketStatusPort;

    @Mock
    private PublishTicketStatusChangedPort publishStatusChangedPort;

    @InjectMocks
    private ChangeTicketStatusUseCase changeTicketStatusUseCase;

    private UUID ticketId;
    private UUID tenantId;
    private UUID userId;
    private Ticket ticket;
    private ChangeTicketStatusCommand command;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        ticket = Ticket.builder()
            .id(ticketId)
            .tenantId(tenantId)
            .clientId(UUID.randomUUID())
            .title("Title")
            .description("Desc")
            .status("OPEN")
            .priority("NORMAL")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        command = ChangeTicketStatusCommand.builder()
            .ticketId(ticketId)
            .tenantId(tenantId)
            .userId(userId)
            .newStatus("IN_PROGRESS")
            .build();
    }

    @Nested
    @DisplayName("changeStatus")
    class ChangeStatus {

        @Test
        @DisplayName("returns empty when ticket not found")
        void returnsEmptyWhenTicketNotFound() {
            when(findTicketPort.findByIdAndTenantId(ticketId, tenantId)).thenReturn(Optional.empty());

            Optional<Ticket> result = changeTicketStatusUseCase.changeStatus(command);

            assertThat(result).isEmpty();
            verify(updateTicketStatusPort, never()).updateStatus(any(), any(), any());
            verify(publishStatusChangedPort, never()).publish(any());
        }

        @Test
        @DisplayName("returns ticket without update or event when status unchanged")
        void noUpdateWhenStatusUnchanged() {
            when(findTicketPort.findByIdAndTenantId(ticketId, tenantId)).thenReturn(Optional.of(ticket));
            command = ChangeTicketStatusCommand.builder()
                .ticketId(ticketId)
                .tenantId(tenantId)
                .userId(userId)
                .newStatus("OPEN")
                .build();

            Optional<Ticket> result = changeTicketStatusUseCase.changeStatus(command);

            assertThat(result).containsSame(ticket);
            verify(updateTicketStatusPort, never()).updateStatus(any(), any(), any());
            verify(publishStatusChangedPort, never()).publish(any());
        }

        @Test
        @DisplayName("updates status, persists history and publishes event when status changes")
        void updatesAndPublishesWhenStatusChanges() {
            when(findTicketPort.findByIdAndTenantId(ticketId, tenantId)).thenReturn(Optional.of(ticket));
            Ticket updated = Ticket.builder()
                .id(ticketId)
                .tenantId(tenantId)
                .clientId(ticket.getClientId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status("IN_PROGRESS")
                .priority(ticket.getPriority())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
            when(updateTicketStatusPort.updateStatus(any(Ticket.class), eq(userId), eq("OPEN"))).thenReturn(updated);

            Optional<Ticket> result = changeTicketStatusUseCase.changeStatus(command);

            assertThat(result).containsSame(updated);
            ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            verify(updateTicketStatusPort).updateStatus(ticketCaptor.capture(), eq(userId), eq("OPEN"));
            assertThat(ticketCaptor.getValue().getStatus()).isEqualTo("IN_PROGRESS");

            ArgumentCaptor<TicketStatusChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(TicketStatusChangedEvent.class);
            verify(publishStatusChangedPort).publish(eventCaptor.capture());
            TicketStatusChangedEvent event = eventCaptor.getValue();
            assertThat(event.getTicketId()).isEqualTo(ticketId);
            assertThat(event.getTenantId()).isEqualTo(tenantId);
            assertThat(event.getUserId()).isEqualTo(userId);
            assertThat(event.getClientId()).isEqualTo(ticket.getClientId());
            assertThat(event.getOldStatus()).isEqualTo("OPEN");
            assertThat(event.getNewStatus()).isEqualTo("IN_PROGRESS");
            assertThat(event.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("trims new status before comparing and updating")
        void trimsNewStatus() {
            when(findTicketPort.findByIdAndTenantId(ticketId, tenantId)).thenReturn(Optional.of(ticket));
            Ticket updated = Ticket.builder()
                .id(ticketId)
                .status("RESOLVED")
                .updatedAt(LocalDateTime.now())
                .build();
            when(updateTicketStatusPort.updateStatus(any(Ticket.class), eq(userId), eq("OPEN"))).thenReturn(updated);
            command = ChangeTicketStatusCommand.builder()
                .ticketId(ticketId)
                .tenantId(tenantId)
                .userId(userId)
                .newStatus("  RESOLVED  ")
                .build();

            changeTicketStatusUseCase.changeStatus(command);

            ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            verify(updateTicketStatusPort).updateStatus(ticketCaptor.capture(), eq(userId), eq("OPEN"));
            assertThat(ticketCaptor.getValue().getStatus()).isEqualTo("RESOLVED");
        }

        @Test
        @DisplayName("throws when new status is null")
        void throwsWhenNewStatusNull() {
            when(findTicketPort.findByIdAndTenantId(ticketId, tenantId)).thenReturn(Optional.of(ticket));
            command = ChangeTicketStatusCommand.builder()
                .ticketId(ticketId)
                .tenantId(tenantId)
                .userId(userId)
                .newStatus(null)
                .build();

            assertThatThrownBy(() -> changeTicketStatusUseCase.changeStatus(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("New status cannot be null or blank");
            verify(updateTicketStatusPort, never()).updateStatus(any(), any(), any());
            verify(publishStatusChangedPort, never()).publish(any());
        }

        @Test
        @DisplayName("throws when new status is blank")
        void throwsWhenNewStatusBlank() {
            when(findTicketPort.findByIdAndTenantId(ticketId, tenantId)).thenReturn(Optional.of(ticket));
            command = ChangeTicketStatusCommand.builder()
                .ticketId(ticketId)
                .tenantId(tenantId)
                .userId(userId)
                .newStatus("   ")
                .build();

            assertThatThrownBy(() -> changeTicketStatusUseCase.changeStatus(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("New status cannot be null or blank");
            verify(updateTicketStatusPort, never()).updateStatus(any(), any(), any());
            verify(publishStatusChangedPort, never()).publish(any());
        }
    }
}
