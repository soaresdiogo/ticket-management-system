package com.di2it.ticket_service.application.port;

import com.di2it.ticket_service.domain.entity.Ticket;

import java.util.UUID;

/**
 * Port for persisting a new ticket and its initial status history.
 */
@FunctionalInterface
public interface CreateTicketPort {

    /**
     * Saves the given ticket and records the initial status in history.
     *
     * @param ticket the ticket to persist (id may be null; will be generated)
     * @param changedBy userId who created the ticket (for status history)
     * @return the persisted ticket with id and timestamps set
     */
    Ticket save(Ticket ticket, UUID changedBy);
}
