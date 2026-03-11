package com.di2it.file_service.infrastructure.persistence;

import com.di2it.file_service.application.port.SaveAttachmentPort;
import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.domain.repository.AttachmentRepository;

import org.springframework.stereotype.Component;

/**
 * JPA adapter for persisting attachment metadata. Delegates to AttachmentRepository.
 */
@Component
public class SaveAttachmentAdapter implements SaveAttachmentPort {

    private final AttachmentRepository attachmentRepository;

    public SaveAttachmentAdapter(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public Attachment save(Attachment attachment) {
        return attachmentRepository.save(attachment);
    }
}
