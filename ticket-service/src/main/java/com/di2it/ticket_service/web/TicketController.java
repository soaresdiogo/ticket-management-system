package com.di2it.ticket_service.web;

import com.di2it.ticket_service.application.usecase.AddCommentCommand;
import com.di2it.ticket_service.application.usecase.AddCommentUseCase;
import com.di2it.ticket_service.application.usecase.ChangeTicketStatusCommand;
import com.di2it.ticket_service.application.usecase.ChangeTicketStatusUseCase;
import com.di2it.ticket_service.application.usecase.CreateTicketCommand;
import com.di2it.ticket_service.application.usecase.CreateTicketUseCase;
import com.di2it.ticket_service.application.usecase.ListAllTicketsUseCase;
import com.di2it.ticket_service.application.usecase.ListTicketsUseCase;
import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.web.dto.AddCommentRequest;
import com.di2it.ticket_service.web.dto.AddCommentResponse;
import com.di2it.ticket_service.web.dto.ChangeTicketStatusRequest;
import com.di2it.ticket_service.web.dto.ChangeTicketStatusResponse;
import com.di2it.ticket_service.web.dto.CreateTicketRequest;
import com.di2it.ticket_service.web.dto.CreateTicketResponse;
import com.di2it.ticket_service.web.dto.ListTicketsResponse;
import com.di2it.ticket_service.web.mapper.AddCommentResponseMapper;
import com.di2it.ticket_service.web.mapper.ChangeTicketStatusResponseMapper;
import com.di2it.ticket_service.web.mapper.CreateTicketResponseMapper;
import com.di2it.ticket_service.web.mapper.ListTicketsResponseMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for ticket operations.
 * Expects X-User-Id and X-Tenant-Id headers from the API Gateway (set after JWT validation).
 */
@Tag(name = "Tickets", description = "Create and manage support tickets")
@RestController
@RequestMapping("/tickets")
public class TicketController {

    private static final String DEFAULT_SORT_FIELD = "createdAt";
    private static final String API_RESPONSE_UNAUTHORIZED = "401";
    private static final String SECURITY_SCHEME_BEARER_JWT = "bearer-jwt";

    private final CreateTicketUseCase createTicketUseCase;
    private final ListTicketsUseCase listTicketsUseCase;
    private final ListAllTicketsUseCase listAllTicketsUseCase;
    private final ChangeTicketStatusUseCase changeTicketStatusUseCase;
    private final AddCommentUseCase addCommentUseCase;

    public TicketController(
        CreateTicketUseCase createTicketUseCase,
        ListTicketsUseCase listTicketsUseCase,
        ListAllTicketsUseCase listAllTicketsUseCase,
        ChangeTicketStatusUseCase changeTicketStatusUseCase,
        AddCommentUseCase addCommentUseCase
    ) {
        this.createTicketUseCase = createTicketUseCase;
        this.listTicketsUseCase = listTicketsUseCase;
        this.listAllTicketsUseCase = listAllTicketsUseCase;
        this.changeTicketStatusUseCase = changeTicketStatusUseCase;
        this.addCommentUseCase = addCommentUseCase;
    }

    /**
     * Create a new ticket. Client and tenant are taken from gateway-propagated headers.
     */
    @Operation(
        summary = "Create ticket",
        description = "Creates a new ticket. Requires X-User-Id and X-Tenant-Id from gateway.")
    @ApiResponse(responseCode = "201", description = "Ticket created")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = API_RESPONSE_UNAUTHORIZED, description = "Missing or invalid user context")
    @SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
    @PostMapping
    public ResponseEntity<CreateTicketResponse> createTicket(
        @RequestHeader(WebConstants.HEADER_USER_ID) UUID clientId,
        @RequestHeader(WebConstants.HEADER_TENANT_ID) UUID tenantId,
        @Valid @RequestBody CreateTicketRequest request
    ) {
        CreateTicketCommand command = CreateTicketCommand.builder()
            .tenantId(tenantId)
            .clientId(clientId)
            .title(request.getTitle())
            .description(request.getDescription())
            .priority(request.getPriority())
            .category(request.getCategory())
            .build();

        Ticket ticket = createTicketUseCase.create(command);
        CreateTicketResponse response = CreateTicketResponseMapper.toResponse(ticket);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List tickets for the current user (client). Filtered by JWT userId (X-User-Id).
     */
    @Operation(
        summary = "List my tickets",
        description = "Paginated list of tickets for the authenticated user. Requires X-User-Id. Newest first.")
    @ApiResponse(responseCode = "200", description = "Paginated list of tickets")
    @ApiResponse(responseCode = API_RESPONSE_UNAUTHORIZED, description = "Missing or invalid user context")
    @SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
    @GetMapping
    public ResponseEntity<ListTicketsResponse> listTickets(
        @RequestHeader(WebConstants.HEADER_USER_ID) UUID clientId,
        @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, DEFAULT_SORT_FIELD));
        ListTicketsResponse response = ListTicketsResponseMapper.toResponse(
            listTicketsUseCase.listByClient(clientId, pageable));
        return ResponseEntity.ok(response);
    }

    /**
     * List all tickets for the tenant. Restricted to ACCOUNTANT role (X-User-Role from gateway).
     */
    @Operation(
        summary = "List all tickets (ACCOUNTANT)",
        description = "Paginated list of all tickets for the tenant. Requires ACCOUNTANT and X-Tenant-Id.")
    @ApiResponse(responseCode = "200", description = "Paginated list of tickets")
    @ApiResponse(responseCode = "403", description = "Forbidden: requires ACCOUNTANT role")
    @ApiResponse(responseCode = API_RESPONSE_UNAUTHORIZED,
        description = "Missing or invalid user context (tenant/role)")
    @SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
    @GetMapping("/all")
    public ResponseEntity<ListTicketsResponse> listAllTickets(
        @RequestHeader(value = WebConstants.HEADER_USER_ROLE, required = false) String role,
        @RequestHeader(WebConstants.HEADER_TENANT_ID) UUID tenantId,
        @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Optional status filter (e.g. OPEN, IN_PROGRESS, RESOLVED, CLOSED)")
        @RequestParam(required = false) String status
    ) {
        if (!WebConstants.ROLE_ACCOUNTANT.equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, DEFAULT_SORT_FIELD));
        ListTicketsResponse response = ListTicketsResponseMapper.toResponse(
            listAllTicketsUseCase.listByTenant(tenantId, pageable, status));
        return ResponseEntity.ok(response);
    }

    /**
     * Change ticket status. Restricted to ACCOUNTANT role; ticket must belong to the tenant.
     * On status change, publishes event to Kafka topic ticket.status.changed.
     */
    @Operation(
        summary = "Change ticket status (ACCOUNTANT)",
        description = "Updates ticket status. Requires ACCOUNTANT. Publishes to Kafka. 404 if not found.")
    @ApiResponse(responseCode = "200", description = "Status updated (or unchanged)")
    @ApiResponse(responseCode = "400", description = "Invalid request body (e.g. blank status)")
    @ApiResponse(responseCode = "403", description = "Forbidden: requires ACCOUNTANT role")
    @ApiResponse(responseCode = "404", description = "Ticket not found or not in tenant")
    @SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
    @PatchMapping("/{id}/status")
    public ResponseEntity<ChangeTicketStatusResponse> changeTicketStatus(
        @Parameter(description = "Ticket ID") @PathVariable UUID id,
        @RequestHeader(WebConstants.HEADER_USER_ID) UUID userId,
        @RequestHeader(value = WebConstants.HEADER_USER_ROLE, required = false) String role,
        @RequestHeader(WebConstants.HEADER_TENANT_ID) UUID tenantId,
        @Valid @RequestBody ChangeTicketStatusRequest request
    ) {
        if (!WebConstants.ROLE_ACCOUNTANT.equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ChangeTicketStatusCommand command = ChangeTicketStatusCommand.builder()
            .ticketId(id)
            .tenantId(tenantId)
            .userId(userId)
            .newStatus(request.getStatus())
            .build();
        return changeTicketStatusUseCase.changeStatus(command)
            .map(ChangeTicketStatusResponseMapper::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Add a comment to a ticket. Ticket must belong to the tenant (from X-Tenant-Id).
     * Both clients and accountants can add comments; author is taken from X-User-Id and X-User-Role.
     */
    @Operation(
        summary = "Add comment",
        description = "Adds a comment to a ticket. Requires X-User-Id, X-Tenant-Id. Returns 404 if not found.")
    @ApiResponse(responseCode = "201", description = "Comment created")
    @ApiResponse(responseCode = "400", description = "Invalid request body (e.g. blank content)")
    @ApiResponse(responseCode = API_RESPONSE_UNAUTHORIZED, description = "Missing or invalid user context")
    @ApiResponse(responseCode = "404", description = "Ticket not found or not in tenant")
    @SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
    @PostMapping("/{id}/comments")
    public ResponseEntity<AddCommentResponse> addComment(
        @Parameter(description = "Ticket ID") @PathVariable UUID id,
        @RequestHeader(WebConstants.HEADER_USER_ID) UUID authorId,
        @RequestHeader(value = WebConstants.HEADER_USER_ROLE, required = false) String authorRole,
        @RequestHeader(WebConstants.HEADER_TENANT_ID) UUID tenantId,
        @Valid @RequestBody AddCommentRequest request
    ) {
        AddCommentCommand command = AddCommentCommand.builder()
            .ticketId(id)
            .tenantId(tenantId)
            .authorId(authorId)
            .authorRole(authorRole)
            .content(request.getContent())
            .internal(request.isInternal())
            .build();
        return addCommentUseCase.addComment(command)
            .map(AddCommentResponseMapper::toResponse)
            .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
            .orElse(ResponseEntity.notFound().build());
    }
}
