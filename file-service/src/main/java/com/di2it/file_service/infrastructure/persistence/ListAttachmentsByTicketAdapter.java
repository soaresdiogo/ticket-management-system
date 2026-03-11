package com.di2it.file_service.infrastructure.persistence;

import com.di2it.file_service.application.port.ListAttachmentsByTicketPort;
import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.domain.repository.AttachmentRepository;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * JPA adapter for listing attachments by ticket and tenant.
 */
@Component
public class ListAttachmentsByTicketAdapter implements ListAttachmentsByTicketPort {

    private final AttachmentRepository attachmentRepository;

    public ListAttachmentsByTicketAdapter(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public List<Attachment> findByTicketIdAndTenantId(UUID ticketId, UUID tenantId) {
        return attachmentRepository.findByTicketIdAndTenantIdAndDeletedAtIsNullOrderByCreatedAtDesc(ticketId, tenantId);
    }
}
