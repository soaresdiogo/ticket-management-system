package com.di2it.file_service.web.mapper;

import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.web.dto.UploadFileResponse;

/**
 * Maps Attachment entity to UploadFileResponse DTO.
 */
public final class UploadFileResponseMapper {

    private UploadFileResponseMapper() {
    }

    public static UploadFileResponse toResponse(Attachment attachment) {
        if (attachment == null) {
            return null;
        }
        return new UploadFileResponse(
            attachment.getId(),
            attachment.getTicketId(),
            attachment.getFileName(),
            attachment.getMimeType(),
            attachment.getFileSize(),
            attachment.getCreatedAt()
        );
    }
}
