package com.di2it.ticket_service.application.usecase;

import com.di2it.ticket_service.application.port.ListTicketsByClientPort;
import com.di2it.ticket_service.domain.entity.Ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case: list tickets for the current user (client). Results are filtered by clientId from JWT.
 */
@Service
public class ListTicketsUseCase {

    private final ListTicketsByClientPort listTicketsByClientPort;

    public ListTicketsUseCase(ListTicketsByClientPort listTicketsByClientPort) {
        this.listTicketsByClientPort = listTicketsByClientPort;
    }

    /**
     * Returns a page of tickets for the given client.
     *
     * @param clientId the authenticated client's user id (from JWT)
     * @param pageable pagination (page index and size)
     * @return page of tickets belonging to the client
     */
    public Page<Ticket> listByClient(UUID clientId, Pageable pageable) {
        return listTicketsByClientPort.findByClientId(clientId, pageable);
    }
}
