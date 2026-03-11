package com.di2it.file_service.application.usecase;

import com.di2it.file_service.application.port.FindAttachmentPort;
import com.di2it.file_service.application.port.ObjectStoragePort;
import com.di2it.file_service.application.port.SaveDownloadLogPort;
import com.di2it.file_service.config.MinioProperties;
import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.domain.entity.DownloadLog;
import com.di2it.file_service.domain.exception.AttachmentNotFoundException;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Use case: resolve presigned download URL for an attachment (by id and tenant), optionally log the download.
 */
@Service
public class GetFileDownloadUrlUseCase {

    private final FindAttachmentPort findAttachmentPort;
    private final ObjectStoragePort objectStoragePort;
    private final SaveDownloadLogPort saveDownloadLogPort;
    private final MinioProperties minioProperties;

    public GetFileDownloadUrlUseCase(
        FindAttachmentPort findAttachmentPort,
        ObjectStoragePort objectStoragePort,
        SaveDownloadLogPort saveDownloadLogPort,
        MinioProperties minioProperties
    ) {
        this.findAttachmentPort = findAttachmentPort;
        this.objectStoragePort = objectStoragePort;
        this.saveDownloadLogPort = saveDownloadLogPort;
        this.minioProperties = minioProperties;
    }

    /**
     * Returns presigned URL for the attachment if it exists and belongs to the tenant; logs the download.
     *
     * @param attachmentId  attachment id
     * @param tenantId      tenant id (from header)
     * @param downloadedBy  user id performing the download (from header)
     * @param ipAddress     optional client IP for audit
     * @return result with url and expiry seconds
     * @throws AttachmentNotFoundException if attachment not found or not in tenant
     */
    public FileDownloadResult getDownloadUrl(
        UUID attachmentId,
        UUID tenantId,
        UUID downloadedBy,
        Optional<String> ipAddress
    ) {
        Attachment attachment = findAttachmentPort.findByIdAndTenantId(attachmentId, tenantId)
            .orElseThrow(() -> new AttachmentNotFoundException("Attachment not found: " + attachmentId));

        String presignedUrl = objectStoragePort.getPresignedDownloadUrl(attachment.getMinioKey());

        DownloadLog log = DownloadLog.builder()
            .attachment(attachment)
            .downloadedBy(downloadedBy)
            .ipAddress(ipAddress.orElse(null))
            .build();
        saveDownloadLogPort.save(log);

        return new FileDownloadResult(
            presignedUrl,
            minioProperties.getPresignedExpirySeconds(),
            attachment.getFileName()
        );
    }

    /**
     * Result of generating a presigned download URL.
     */
    public record FileDownloadResult(String url, int expiresInSeconds, String fileName) {
    }
}
