package com.di2it.ticket_service.web;

import com.di2it.ticket_service.application.usecase.CreateTicketUseCase;
import com.di2it.ticket_service.application.usecase.ListTicketsUseCase;
import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.web.dto.CreateTicketRequest;
import com.di2it.ticket_service.web.dto.CreateTicketResponse;
import com.di2it.ticket_service.web.dto.ListTicketsResponse;
import com.di2it.ticket_service.web.dto.TicketListItemResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private CreateTicketUseCase createTicketUseCase;

    @Mock
    private ListTicketsUseCase listTicketsUseCase;

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

    @Nested
    @DisplayName("listTickets")
    class ListTickets {

        @Test
        @DisplayName("returns 200 and paginated list when user has tickets")
        void returns200AndPaginatedList() {
            Page<Ticket> ticketPage = new PageImpl<>(
                List.of(savedTicket),
                PageRequest.of(0, 20),
                1
            );
            when(listTicketsUseCase.listByClient(eq(clientId), any())).thenReturn(ticketPage);

            ResponseEntity<ListTicketsResponse> result = ticketController.listTickets(clientId, 0, 20);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            List<TicketListItemResponse> content = result.getBody().getContent();
            assertThat(content).hasSize(1);
            assertThat(content.get(0).getId()).isEqualTo(savedTicket.getId());
            assertThat(content.get(0).getClientId()).isEqualTo(clientId);
            assertThat(content.get(0).getTitle()).isEqualTo("Invoice discrepancy");
            assertThat(result.getBody().getTotalElements()).isEqualTo(1);
            assertThat(result.getBody().getTotalPages()).isEqualTo(1);
            assertThat(result.getBody().getNumber()).isEqualTo(0);
            assertThat(result.getBody().getSize()).isEqualTo(20);
            verify(listTicketsUseCase).listByClient(eq(clientId), any(org.springframework.data.domain.Pageable.class));
        }

        @Test
        @DisplayName("returns 200 and empty content when user has no tickets")
        void returns200AndEmptyContent() {
            Page<Ticket> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
            when(listTicketsUseCase.listByClient(eq(clientId), any())).thenReturn(emptyPage);

            ResponseEntity<ListTicketsResponse> result = ticketController.listTickets(clientId, 0, 20);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getContent()).isEmpty();
            assertThat(result.getBody().getTotalElements()).isEqualTo(0);
            verify(listTicketsUseCase).listByClient(eq(clientId), any(org.springframework.data.domain.Pageable.class));
        }
    }
}
