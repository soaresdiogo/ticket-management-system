package com.di2it.file_service.application.usecase;

import lombok.Builder;
import lombok.Getter;

import java.io.InputStream;
import java.util.UUID;

/**
 * Input for the upload file use case. Contains file metadata and stream from multipart request.
 */
@Getter
@Builder
public class UploadFileCommand {

    private final UUID ticketId;
    private final UUID tenantId;
    private final UUID uploadedBy;
    private final String uploaderRole;
    private final String fileName;
    private final String contentType;
    private final long fileSize;
    private final InputStream inputStream;
    private final String visibleTo;

    public String getVisibleToOrDefault() {
        return visibleTo != null && !visibleTo.isBlank() ? visibleTo.trim() : "ALL";
    }
}
