package com.di2it.ticket_service.web;

import com.di2it.ticket_service.application.usecase.AddCommentUseCase;
import com.di2it.ticket_service.application.usecase.ChangeTicketStatusUseCase;
import com.di2it.ticket_service.application.usecase.CreateTicketUseCase;
import com.di2it.ticket_service.application.usecase.ListAllTicketsUseCase;
import com.di2it.ticket_service.application.usecase.ListTicketsUseCase;
import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.domain.entity.TicketComment;
import com.di2it.ticket_service.web.dto.AddCommentRequest;
import com.di2it.ticket_service.web.dto.AddCommentResponse;
import com.di2it.ticket_service.web.dto.ChangeTicketStatusRequest;
import com.di2it.ticket_service.web.dto.ChangeTicketStatusResponse;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private CreateTicketUseCase createTicketUseCase;

    @Mock
    private ListTicketsUseCase listTicketsUseCase;

    @Mock
    private ListAllTicketsUseCase listAllTicketsUseCase;

    @Mock
    private ChangeTicketStatusUseCase changeTicketStatusUseCase;

    @Mock
    private AddCommentUseCase addCommentUseCase;

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
            assertThat(result.getBody().getTotalElements()).isZero();
            verify(listTicketsUseCase).listByClient(eq(clientId), any(org.springframework.data.domain.Pageable.class));
        }
    }

    @Nested
    @DisplayName("listAllTickets")
    class ListAllTickets {

        @Test
        @DisplayName("returns 200 and paginated list when role is ACCOUNTANT")
        void returns200AndPaginatedListWhenAccountant() {
            Page<Ticket> ticketPage = new PageImpl<>(
                List.of(savedTicket),
                PageRequest.of(0, 20),
                1
            );
            when(listAllTicketsUseCase.listByTenant(eq(tenantId), any(), any())).thenReturn(ticketPage);

            ResponseEntity<ListTicketsResponse> result = ticketController.listAllTickets(
                "ACCOUNTANT", tenantId, 0, 20, null);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getContent()).hasSize(1);
            assertThat(result.getBody().getContent().get(0).getId()).isEqualTo(savedTicket.getId());
            assertThat(result.getBody().getTotalElements()).isEqualTo(1);
            verify(listAllTicketsUseCase).listByTenant(
                eq(tenantId), any(org.springframework.data.domain.Pageable.class), eq(null));
        }

        @Test
        @DisplayName("returns 200 and paginated list when role is USER (office staff)")
        void returns200AndPaginatedListWhenUser() {
            Page<Ticket> ticketPage = new PageImpl<>(
                List.of(savedTicket),
                PageRequest.of(0, 20),
                1
            );
            when(listAllTicketsUseCase.listByTenant(eq(tenantId), any(), any())).thenReturn(ticketPage);

            ResponseEntity<ListTicketsResponse> result = ticketController.listAllTickets(
                "USER", tenantId, 0, 20, null);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            verify(listAllTicketsUseCase).listByTenant(
                eq(tenantId), any(org.springframework.data.domain.Pageable.class), eq(null));
        }

        @Test
        @DisplayName("returns 403 when role is not ACCOUNTANT or USER")
        void returns403WhenNotAccountant() {
            ResponseEntity<ListTicketsResponse> result = ticketController.listAllTickets(
                "CLIENT", tenantId, 0, 20, null);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(result.getBody()).isNull();
            verify(listAllTicketsUseCase, never()).listByTenant(any(), any(), any());
        }

        @Test
        @DisplayName("returns 403 when role is null")
        void returns403WhenRoleNull() {
            ResponseEntity<ListTicketsResponse> result = ticketController.listAllTickets(
                null, tenantId, 0, 20, null);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(result.getBody()).isNull();
            verify(listAllTicketsUseCase, never()).listByTenant(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("changeTicketStatus")
    class ChangeTicketStatus {

        @Test
        @DisplayName("returns 200 and response body when status is changed")
        void returns200AndBodyWhenSuccess() {
            UUID ticketId = savedTicket.getId();
            ChangeTicketStatusRequest request = ChangeTicketStatusRequest.builder()
                .status("IN_PROGRESS")
                .build();
            when(changeTicketStatusUseCase.changeStatus(any())).thenReturn(java.util.Optional.of(savedTicket));

            ResponseEntity<ChangeTicketStatusResponse> result = ticketController.changeTicketStatus(
                ticketId, clientId, "ACCOUNTANT", tenantId, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getId()).isEqualTo(ticketId);
            assertThat(result.getBody().getStatus()).isEqualTo(savedTicket.getStatus());
            assertThat(result.getBody().getUpdatedAt()).isNotNull();
            verify(changeTicketStatusUseCase).changeStatus(any());
        }

        @Test
        @DisplayName("returns 404 when ticket not found")
        void returns404WhenTicketNotFound() {
            UUID ticketId = UUID.randomUUID();
            ChangeTicketStatusRequest request = ChangeTicketStatusRequest.builder()
                .status("RESOLVED")
                .build();
            when(changeTicketStatusUseCase.changeStatus(any())).thenReturn(java.util.Optional.empty());

            ResponseEntity<ChangeTicketStatusResponse> result = ticketController.changeTicketStatus(
                ticketId, clientId, "ACCOUNTANT", tenantId, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.getBody()).isNull();
            verify(changeTicketStatusUseCase).changeStatus(any());
        }

        @Test
        @DisplayName("returns 200 when role is USER (office staff)")
        void returns200WhenUser() {
            ChangeTicketStatusRequest request = ChangeTicketStatusRequest.builder()
                .status("IN_PROGRESS")
                .build();
            when(changeTicketStatusUseCase.changeStatus(any())).thenReturn(java.util.Optional.of(savedTicket));

            ResponseEntity<ChangeTicketStatusResponse> result = ticketController.changeTicketStatus(
                savedTicket.getId(), clientId, "USER", tenantId, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(changeTicketStatusUseCase).changeStatus(any());
        }

        @Test
        @DisplayName("returns 403 when role is not ACCOUNTANT or USER")
        void returns403WhenNotAccountant() {
            ChangeTicketStatusRequest request = ChangeTicketStatusRequest.builder()
                .status("IN_PROGRESS")
                .build();

            ResponseEntity<ChangeTicketStatusResponse> result = ticketController.changeTicketStatus(
                savedTicket.getId(), clientId, "CLIENT", tenantId, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(result.getBody()).isNull();
            verify(changeTicketStatusUseCase, never()).changeStatus(any());
        }

        @Test
        @DisplayName("returns 403 when role is null")
        void returns403WhenRoleNull() {
            ChangeTicketStatusRequest request = ChangeTicketStatusRequest.builder()
                .status("IN_PROGRESS")
                .build();

            ResponseEntity<ChangeTicketStatusResponse> result = ticketController.changeTicketStatus(
                savedTicket.getId(), clientId, null, tenantId, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(result.getBody()).isNull();
            verify(changeTicketStatusUseCase, never()).changeStatus(any());
        }
    }

    @Nested
    @DisplayName("addComment")
    class AddComment {

        @Test
        @DisplayName("returns 201 and response body when comment is added")
        void returns201AndBodyWhenSuccess() {
            AddCommentRequest request = AddCommentRequest.builder()
                .content("Please check the attachment.")
                .internal(false)
                .build();
            TicketComment savedComment = TicketComment.builder()
                .id(UUID.randomUUID())
                .ticket(savedTicket)
                .authorId(clientId)
                .authorRole("CLIENT")
                .content(request.getContent())
                .internal(false)
                .createdAt(LocalDateTime.now())
                .build();
            when(addCommentUseCase.addComment(any())).thenReturn(java.util.Optional.of(savedComment));

            ResponseEntity<AddCommentResponse> result = ticketController.addComment(
                savedTicket.getId(), clientId, "CLIENT", tenantId, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getId()).isEqualTo(savedComment.getId());
            assertThat(result.getBody().getContent()).isEqualTo("Please check the attachment.");
            assertThat(result.getBody().getAuthorId()).isEqualTo(clientId);
            assertThat(result.getBody().getAuthorRole()).isEqualTo("CLIENT");
            assertThat(result.getBody().isInternal()).isFalse();
            assertThat(result.getBody().getCreatedAt()).isNotNull();
            verify(addCommentUseCase).addComment(any());
        }

        @Test
        @DisplayName("returns 404 when ticket not found")
        void returns404WhenTicketNotFound() {
            AddCommentRequest request = AddCommentRequest.builder()
                .content("A comment")
                .internal(false)
                .build();
            when(addCommentUseCase.addComment(any())).thenReturn(java.util.Optional.empty());

            ResponseEntity<AddCommentResponse> result = ticketController.addComment(
                UUID.randomUUID(), clientId, "CLIENT", tenantId, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.getBody()).isNull();
            verify(addCommentUseCase).addComment(any());
        }
    }
}
