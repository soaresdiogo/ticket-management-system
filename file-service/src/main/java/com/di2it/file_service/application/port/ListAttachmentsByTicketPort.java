package com.di2it.file_service.application.port;

import com.di2it.file_service.domain.entity.Attachment;

import java.util.List;
import java.util.UUID;

/**
 * Port for listing attachments for a ticket scoped by tenant.
 */
@FunctionalInterface
public interface ListAttachmentsByTicketPort {

    /**
     * Returns attachments for the given ticket and tenant, excluding soft-deleted, newest first.
     *
     * @param ticketId ticket id
     * @param tenantId tenant id (from JWT/header)
     * @return list of attachments, never null
     */
    List<Attachment> findByTicketIdAndTenantId(UUID ticketId, UUID tenantId);
}
