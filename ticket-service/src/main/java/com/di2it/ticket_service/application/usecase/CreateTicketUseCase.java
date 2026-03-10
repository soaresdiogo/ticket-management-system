package com.di2it.ticket_service.application.usecase;

import com.di2it.ticket_service.application.port.CreateTicketPort;
import com.di2it.ticket_service.domain.entity.Ticket;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Use case: create a new ticket with initial OPEN status and one status history record.
 */
@Service
public class CreateTicketUseCase {

    private static final String INITIAL_STATUS = "OPEN";
    private static final String DEFAULT_PRIORITY = "NORMAL";

    private final CreateTicketPort createTicketPort;

    public CreateTicketUseCase(CreateTicketPort createTicketPort) {
        this.createTicketPort = createTicketPort;
    }

    /**
     * Creates a ticket for the given tenant and client with OPEN status.
     *
     * @param command tenant, client, title, description, optional priority and category
     * @return the persisted ticket
     */
    public Ticket create(CreateTicketCommand command) {
        String priority = command.getPriority() != null && !command.getPriority().isBlank()
            ? command.getPriority().trim().toUpperCase()
            : DEFAULT_PRIORITY;

        Ticket ticket = Ticket.builder()
            .tenantId(command.getTenantId())
            .clientId(command.getClientId())
            .title(command.getTitle().trim())
            .description(command.getDescription().trim())
            .status(INITIAL_STATUS)
            .priority(priority)
            .category(command.getCategory() != null && !command.getCategory().isBlank()
                ? command.getCategory().trim() : null)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        return createTicketPort.save(ticket, command.getClientId());
    }
}
