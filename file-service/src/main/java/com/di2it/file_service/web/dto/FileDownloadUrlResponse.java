package com.di2it.file_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response containing a presigned URL to download a file from object storage.
 */
@Schema(description = "Presigned download URL and metadata")
public record FileDownloadUrlResponse(
    @Schema(description = "Presigned URL valid for limited time", example = "https://minio.example.com/...")
    String url,
    @Schema(description = "URL validity in seconds", example = "900")
    int expiresInSeconds,
    @Schema(description = "Original file name for download", example = "document.pdf")
    String fileName
) {
}
