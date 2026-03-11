package com.di2it.file_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response after successful file upload.
 */
@Schema(description = "Metadata of the uploaded file")
public record UploadFileResponse(
    @Schema(description = "Attachment ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,
    @Schema(description = "Ticket ID the file is attached to")
    UUID ticketId,
    @Schema(description = "Original file name")
    String fileName,
    @Schema(description = "MIME type")
    String mimeType,
    @Schema(description = "File size in bytes")
    Long fileSize,
    @Schema(description = "Upload timestamp")
    LocalDateTime createdAt
) {
}
