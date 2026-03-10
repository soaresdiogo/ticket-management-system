package com.di2it.ticket_service.web;

import com.di2it.ticket_service.application.usecase.CreateTicketCommand;
import com.di2it.ticket_service.application.usecase.CreateTicketUseCase;
import com.di2it.ticket_service.application.usecase.ListAllTicketsUseCase;
import com.di2it.ticket_service.application.usecase.ListTicketsUseCase;
import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.web.dto.CreateTicketRequest;
import com.di2it.ticket_service.web.dto.CreateTicketResponse;
import com.di2it.ticket_service.web.dto.ListTicketsResponse;
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

    private final CreateTicketUseCase createTicketUseCase;
    private final ListTicketsUseCase listTicketsUseCase;
    private final ListAllTicketsUseCase listAllTicketsUseCase;

    public TicketController(
        CreateTicketUseCase createTicketUseCase,
        ListTicketsUseCase listTicketsUseCase,
        ListAllTicketsUseCase listAllTicketsUseCase
    ) {
        this.createTicketUseCase = createTicketUseCase;
        this.listTicketsUseCase = listTicketsUseCase;
        this.listAllTicketsUseCase = listAllTicketsUseCase;
    }

    /**
     * Create a new ticket. Client and tenant are taken from gateway-propagated headers.
     */
    @Operation(
        summary = "Create ticket",
        description = "Creates a new ticket for the authenticated user (client). Requires X-User-Id and X-Tenant-Id headers from the gateway."
    )
    @ApiResponse(responseCode = "201", description = "Ticket created")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "401", description = "Missing or invalid user context (X-User-Id / X-Tenant-Id)")
    @SecurityRequirement(name = "bearer-jwt")
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
        description = "Returns a paginated list of tickets for the authenticated user (client). Requires X-User-Id from the gateway. Results are ordered by creation date, newest first."
    )
    @ApiResponse(responseCode = "200", description = "Paginated list of tickets")
    @ApiResponse(responseCode = "401", description = "Missing or invalid user context (X-User-Id)")
    @SecurityRequirement(name = "bearer-jwt")
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
        description = "Returns a paginated list of all tickets for the tenant. Requires X-User-Role=ACCOUNTANT and X-Tenant-Id from the gateway. Results are ordered by creation date, newest first."
    )
    @ApiResponse(responseCode = "200", description = "Paginated list of tickets")
    @ApiResponse(responseCode = "403", description = "Forbidden: requires ACCOUNTANT role")
    @ApiResponse(responseCode = "401", description = "Missing or invalid user context (X-Tenant-Id / X-User-Role)")
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/all")
    public ResponseEntity<ListTicketsResponse> listAllTickets(
        @RequestHeader(value = WebConstants.HEADER_USER_ROLE, required = false) String role,
        @RequestHeader(WebConstants.HEADER_TENANT_ID) UUID tenantId,
        @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        if (!WebConstants.ROLE_ACCOUNTANT.equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, DEFAULT_SORT_FIELD));
        ListTicketsResponse response = ListTicketsResponseMapper.toResponse(
            listAllTicketsUseCase.listByTenant(tenantId, pageable));
        return ResponseEntity.ok(response);
    }
}
