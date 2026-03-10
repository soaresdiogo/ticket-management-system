package com.di2it.ticket_service.infrastructure.persistence;

import com.di2it.ticket_service.application.port.FindTicketByIdAndTenantIdPort;
import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.domain.repository.TicketRepository;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that loads a ticket by id and tenant id using the repository.
 */
@Component
public class FindTicketByIdAndTenantIdAdapter implements FindTicketByIdAndTenantIdPort {

    private final TicketRepository ticketRepository;

    public FindTicketByIdAndTenantIdAdapter(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Optional<Ticket> findByIdAndTenantId(UUID id, UUID tenantId) {
        return ticketRepository.findByIdAndTenantId(id, tenantId);
    }
}
