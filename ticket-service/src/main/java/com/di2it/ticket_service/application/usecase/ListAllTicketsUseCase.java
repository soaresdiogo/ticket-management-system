package com.di2it.ticket_service.application.usecase;

import com.di2it.ticket_service.application.port.ListAllTicketsPort;
import com.di2it.ticket_service.domain.entity.Ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case: list all tickets for a tenant (e.g. for ACCOUNTANT role).
 * Caller must ensure the requester has ACCOUNTANT role before invoking.
 */
@Service
public class ListAllTicketsUseCase {

    private final ListAllTicketsPort listAllTicketsPort;

    public ListAllTicketsUseCase(ListAllTicketsPort listAllTicketsPort) {
        this.listAllTicketsPort = listAllTicketsPort;
    }

    /**
     * Returns a page of all tickets for the given tenant.
     *
     * @param tenantId tenant id from gateway (X-Tenant-Id)
     * @param pageable pagination (page index and size)
     * @return page of tickets for the tenant
     */
    public Page<Ticket> listByTenant(UUID tenantId, Pageable pageable) {
        return listAllTicketsPort.findByTenantId(tenantId, pageable);
    }
}
