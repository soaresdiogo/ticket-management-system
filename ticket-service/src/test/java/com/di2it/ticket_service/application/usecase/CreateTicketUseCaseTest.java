package com.di2it.ticket_service.application.usecase;

import com.di2it.ticket_service.application.port.CreateTicketPort;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTicketUseCaseTest {

    @Mock
    private CreateTicketPort createTicketPort;

    @InjectMocks
    private CreateTicketUseCase createTicketUseCase;

    private UUID tenantId;
    private UUID clientId;
    private CreateTicketCommand command;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        command = CreateTicketCommand.builder()
            .tenantId(tenantId)
            .clientId(clientId)
            .title("  Invoice issue  ")
            .description("Description of the problem")
            .priority("HIGH")
            .category("Billing")
            .build();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("builds ticket with OPEN status and delegates to port")
        void buildsTicketAndDelegatesToPort() {
            Ticket saved = Ticket.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .clientId(clientId)
                .title("Invoice issue")
                .description("Description of the problem")
                .status("OPEN")
                .priority("HIGH")
                .category("Billing")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            when(createTicketPort.save(any(Ticket.class), eq(clientId))).thenReturn(saved);

            Ticket result = createTicketUseCase.create(command);

            assertThat(result).isSameAs(saved);
            ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            verify(createTicketPort).save(ticketCaptor.capture(), eq(clientId));
            Ticket passed = ticketCaptor.getValue();
            assertThat(passed.getTenantId()).isEqualTo(tenantId);
            assertThat(passed.getClientId()).isEqualTo(clientId);
            assertThat(passed.getTitle()).isEqualTo("Invoice issue");
            assertThat(passed.getDescription()).isEqualTo("Description of the problem");
            assertThat(passed.getStatus()).isEqualTo("OPEN");
            assertThat(passed.getPriority()).isEqualTo("HIGH");
            assertThat(passed.getCategory()).isEqualTo("Billing");
        }

        @Test
        @DisplayName("uses default priority when priority is null")
        void usesDefaultPriorityWhenNull() {
            CreateTicketCommand cmd = CreateTicketCommand.builder()
                .tenantId(tenantId)
                .clientId(clientId)
                .title("Title")
                .description("Desc")
                .priority(null)
                .category(null)
                .build();
            Ticket saved = Ticket.builder().id(UUID.randomUUID()).build();
            when(createTicketPort.save(any(Ticket.class), eq(clientId))).thenReturn(saved);

            createTicketUseCase.create(cmd);

            ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            verify(createTicketPort).save(ticketCaptor.capture(), eq(clientId));
            assertThat(ticketCaptor.getValue().getPriority()).isEqualTo("NORMAL");
            assertThat(ticketCaptor.getValue().getCategory()).isNull();
        }

        @Test
        @DisplayName("uses default priority when priority is blank")
        void usesDefaultPriorityWhenBlank() {
            CreateTicketCommand cmd = CreateTicketCommand.builder()
                .tenantId(tenantId)
                .clientId(clientId)
                .title("Title")
                .description("Desc")
                .priority("   ")
                .category("")
                .build();
            Ticket saved = Ticket.builder().id(UUID.randomUUID()).build();
            when(createTicketPort.save(any(Ticket.class), eq(clientId))).thenReturn(saved);

            createTicketUseCase.create(cmd);

            ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            verify(createTicketPort).save(ticketCaptor.capture(), eq(clientId));
            assertThat(ticketCaptor.getValue().getPriority()).isEqualTo("NORMAL");
            assertThat(ticketCaptor.getValue().getCategory()).isNull();
        }
    }
}
