package com.di2it.ticket_service.application.port;

import com.di2it.ticket_service.domain.entity.Ticket;

import java.util.UUID;

/**
 * Port for updating a ticket's status and recording the change in history.
 */
public interface UpdateTicketStatusPort {

    /**
     * Persists the ticket with its new status and appends a status history record.
     *
     * @param ticket    ticket with status already set to the new value
     * @param changedBy user id who performed the change
     * @param oldStatus previous status (for history)
     * @return the persisted ticket
     */
    Ticket updateStatus(Ticket ticket, UUID changedBy, String oldStatus);
}
