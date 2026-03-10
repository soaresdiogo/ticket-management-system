package com.di2it.ticket_service.application.port;

import com.di2it.ticket_service.domain.entity.Ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Port to list tickets for a given client (current user).
 */
@FunctionalInterface
public interface ListTicketsByClientPort {

    Page<Ticket> findByClientId(UUID clientId, Pageable pageable);
}
