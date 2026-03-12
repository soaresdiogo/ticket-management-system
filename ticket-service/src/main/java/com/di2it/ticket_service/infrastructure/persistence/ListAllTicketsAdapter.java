package com.di2it.ticket_service.infrastructure.persistence;

import com.di2it.ticket_service.application.port.ListAllTicketsPort;
import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.domain.repository.TicketRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter that lists all tickets for a tenant using the ticket repository.
 */
@Component
public class ListAllTicketsAdapter implements ListAllTicketsPort {

    private final TicketRepository ticketRepository;

    public ListAllTicketsAdapter(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Page<Ticket> findByTenantId(UUID tenantId, Pageable pageable) {
        return ticketRepository.findByTenantId(tenantId, pageable);
    }

    @Override
    public Page<Ticket> findByTenantIdAndStatus(UUID tenantId, String status, Pageable pageable) {
        return ticketRepository.findByTenantIdAndStatus(tenantId, status, pageable);
    }
}
