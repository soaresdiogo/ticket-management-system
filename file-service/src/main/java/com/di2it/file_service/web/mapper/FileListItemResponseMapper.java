package com.di2it.file_service.web.mapper;

import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.web.dto.FileListItemResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps Attachment entity to FileListItemResponse DTO.
 */
public final class FileListItemResponseMapper {

    private FileListItemResponseMapper() {
    }

    public static FileListItemResponse toResponse(Attachment attachment) {
        if (attachment == null) {
            return null;
        }
        return new FileListItemResponse(
            attachment.getId(),
            attachment.getTicketId(),
            attachment.getFileName(),
            attachment.getMimeType(),
            attachment.getFileSize(),
            attachment.getCreatedAt()
        );
    }

    public static List<FileListItemResponse> toResponseList(List<Attachment> attachments) {
        if (attachments == null) {
            return List.of();
        }
        return attachments.stream()
            .map(FileListItemResponseMapper::toResponse)
            .collect(Collectors.toList());
    }
}
