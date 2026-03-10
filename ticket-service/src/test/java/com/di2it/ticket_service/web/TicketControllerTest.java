package com.di2it.ticket_service.web;

import com.di2it.ticket_service.application.usecase.CreateTicketUseCase;
import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.web.dto.CreateTicketRequest;
import com.di2it.ticket_service.web.dto.CreateTicketResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private CreateTicketUseCase createTicketUseCase;

    @InjectMocks
    private TicketController ticketController;

    private UUID clientId;
    private UUID tenantId;
    private CreateTicketRequest request;
    private Ticket savedTicket;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        request = CreateTicketRequest.builder()
            .title("Invoice discrepancy")
            .description("Amount does not match.")
            .priority("NORMAL")
            .category("Billing")
            .build();
        savedTicket = Ticket.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .clientId(clientId)
            .title(request.getTitle())
            .description(request.getDescription())
            .status("OPEN")
            .priority(request.getPriority())
            .category(request.getCategory())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("createTicket")
    class CreateTicket {

        @Test
        @DisplayName("returns 201 and response body when creation succeeds")
        void returns201AndBody() {
            when(createTicketUseCase.create(any())).thenReturn(savedTicket);

            ResponseEntity<CreateTicketResponse> result = ticketController.createTicket(
                clientId, tenantId, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getId()).isEqualTo(savedTicket.getId());
            assertThat(result.getBody().getTenantId()).isEqualTo(tenantId);
            assertThat(result.getBody().getClientId()).isEqualTo(clientId);
            assertThat(result.getBody().getTitle()).isEqualTo("Invoice discrepancy");
            assertThat(result.getBody().getDescription()).isEqualTo("Amount does not match.");
            assertThat(result.getBody().getStatus()).isEqualTo("OPEN");
            assertThat(result.getBody().getPriority()).isEqualTo("NORMAL");
            assertThat(result.getBody().getCategory()).isEqualTo("Billing");
            assertThat(result.getBody().getCreatedAt()).isNotNull();
            verify(createTicketUseCase).create(any());
        }
    }
}
