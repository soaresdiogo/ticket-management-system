package com.di2it.ticket_service.infrastructure.persistence;

import com.di2it.ticket_service.application.port.ListTicketsByClientPort;
import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.domain.repository.TicketRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter that lists tickets by client using the ticket repository.
 */
@Component
public class ListTicketsByClientAdapter implements ListTicketsByClientPort {

    private final TicketRepository ticketRepository;

    public ListTicketsByClientAdapter(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Page<Ticket> findByClientId(UUID clientId, Pageable pageable) {
        return ticketRepository.findByClientId(clientId, pageable);
    }
}
