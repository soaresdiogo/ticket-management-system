package com.di2it.ticket_service.application.port;

import com.di2it.ticket_service.domain.entity.Ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Port to list all tickets for a tenant (e.g. for ACCOUNTANT role).
 * Used by GET /tickets/all to return a tenant-scoped list.
 */
public interface ListAllTicketsPort {

    Page<Ticket> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Ticket> findByTenantIdAndStatus(UUID tenantId, String status, Pageable pageable);
}
