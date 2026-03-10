package com.di2it.ticket_service.application.usecase;

import com.di2it.ticket_service.application.event.TicketStatusChangedEvent;
import com.di2it.ticket_service.application.port.FindTicketByIdAndTenantIdPort;
import com.di2it.ticket_service.application.port.PublishTicketStatusChangedPort;
import com.di2it.ticket_service.application.port.UpdateTicketStatusPort;
import com.di2it.ticket_service.domain.entity.Ticket;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Use case: change a ticket's status, persist history, and publish status-changed event.
 * Only performs update and publishes event when the status actually changes.
 */
@Service
public class ChangeTicketStatusUseCase {

    private final FindTicketByIdAndTenantIdPort findTicketPort;
    private final UpdateTicketStatusPort updateTicketStatusPort;
    private final PublishTicketStatusChangedPort publishStatusChangedPort;

    public ChangeTicketStatusUseCase(
        FindTicketByIdAndTenantIdPort findTicketPort,
        UpdateTicketStatusPort updateTicketStatusPort,
        PublishTicketStatusChangedPort publishStatusChangedPort
    ) {
        this.findTicketPort = findTicketPort;
        this.updateTicketStatusPort = updateTicketStatusPort;
        this.publishStatusChangedPort = publishStatusChangedPort;
    }

    /**
     * Changes the ticket status if the ticket exists in the tenant and the status is different.
     *
     * @param command ticketId, tenantId, userId (who changes), newStatus
     * @return updated ticket if found and status was changed or unchanged; empty if ticket not found
     */
    public Optional<Ticket> changeStatus(ChangeTicketStatusCommand command) {
        Optional<Ticket> optionalTicket = findTicketPort.findByIdAndTenantId(
            command.getTicketId(), command.getTenantId());

        if (optionalTicket.isEmpty()) {
            return Optional.empty();
        }

        Ticket ticket = optionalTicket.get();
        String oldStatus = ticket.getStatus();
        String newStatus = normalizeStatus(command.getNewStatus());

        if (oldStatus.equals(newStatus)) {
            return Optional.of(ticket);
        }

        ticket.setStatus(newStatus);
        Ticket updated = updateTicketStatusPort.updateStatus(ticket, command.getUserId(), oldStatus);

        TicketStatusChangedEvent event = TicketStatusChangedEvent.builder()
            .ticketId(updated.getId())
            .userId(command.getUserId())
            .oldStatus(oldStatus)
            .newStatus(newStatus)
            .timestamp(Instant.now())
            .build();
        publishStatusChangedPort.publish(event);

        return Optional.of(updated);
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("New status cannot be null or blank");
        }
        return status.trim();
    }
}
