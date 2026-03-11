package com.di2it.file_service.application.usecase;

import com.di2it.file_service.application.port.ListAttachmentsByTicketPort;
import com.di2it.file_service.domain.entity.Attachment;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Use case: list all attachments for a ticket scoped by tenant.
 */
@Service
public class ListFilesByTicketUseCase {

    private final ListAttachmentsByTicketPort listAttachmentsByTicketPort;

    public ListFilesByTicketUseCase(ListAttachmentsByTicketPort listAttachmentsByTicketPort) {
        this.listAttachmentsByTicketPort = listAttachmentsByTicketPort;
    }

    /**
     * Returns attachments for the given ticket and tenant (excluding soft-deleted), newest first.
     *
     * @param ticketId ticket id
     * @param tenantId tenant id (from header)
     * @return list of attachments, never null
     */
    public List<Attachment> listByTicket(UUID ticketId, UUID tenantId) {
        return listAttachmentsByTicketPort.findByTicketIdAndTenantId(ticketId, tenantId);
    }
}
