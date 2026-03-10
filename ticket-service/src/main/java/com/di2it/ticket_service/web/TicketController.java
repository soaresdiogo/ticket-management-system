package com.di2it.ticket_service.web;

import com.di2it.ticket_service.application.usecase.CreateTicketCommand;
import com.di2it.ticket_service.application.usecase.CreateTicketUseCase;
import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.web.dto.CreateTicketRequest;
import com.di2it.ticket_service.web.dto.CreateTicketResponse;
import com.di2it.ticket_service.web.mapper.CreateTicketResponseMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
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

    private final CreateTicketUseCase createTicketUseCase;

    public TicketController(CreateTicketUseCase createTicketUseCase) {
        this.createTicketUseCase = createTicketUseCase;
    }

    /**
     * Create a new ticket. Client and tenant are taken from gateway-propagated headers.
     */
    @Operation(
        summary = "Create ticket",
        description = "Creates a new ticket for the authenticated user (client). Requires X-User-Id and X-Tenant-Id headers from the gateway."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ticket created"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid user context (X-User-Id / X-Tenant-Id)")
    })
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
}
