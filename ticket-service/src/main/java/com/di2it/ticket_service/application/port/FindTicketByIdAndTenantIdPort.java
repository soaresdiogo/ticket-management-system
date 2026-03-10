package com.di2it.ticket_service.application.port;

import com.di2it.ticket_service.domain.entity.Ticket;

import java.util.Optional;
import java.util.UUID;

/**
 * Port for loading a ticket by id within a tenant (tenant isolation).
 */
@FunctionalInterface
public interface FindTicketByIdAndTenantIdPort {

    /**
     * Finds a ticket by id and tenant id.
     *
     * @param id       ticket id
     * @param tenantId tenant id (from gateway)
     * @return the ticket if found and belongs to tenant, empty otherwise
     */
    Optional<Ticket> findByIdAndTenantId(UUID id, UUID tenantId);
}
