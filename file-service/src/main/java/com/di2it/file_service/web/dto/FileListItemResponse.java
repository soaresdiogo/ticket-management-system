package com.di2it.file_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Summary of an attachment for listing files by ticket.
 */
@Schema(description = "Attachment metadata for list view")
public record FileListItemResponse(
    @Schema(description = "Attachment ID")
    UUID id,
    @Schema(description = "Ticket ID")
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
