package com.di2it.file_service.infrastructure.persistence;

import com.di2it.file_service.application.port.FindAttachmentPort;
import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.domain.repository.AttachmentRepository;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter for loading an attachment by id and tenant.
 */
@Component
public class FindAttachmentAdapter implements FindAttachmentPort {

    private final AttachmentRepository attachmentRepository;

    public FindAttachmentAdapter(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public Optional<Attachment> findByIdAndTenantId(UUID id, UUID tenantId) {
        return attachmentRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId);
    }
}
