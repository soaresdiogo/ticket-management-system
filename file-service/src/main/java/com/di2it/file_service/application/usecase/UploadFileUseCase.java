package com.di2it.file_service.application.usecase;

import com.di2it.file_service.application.event.TicketDocumentUploadedEvent;
import com.di2it.file_service.application.port.ObjectStoragePort;
import com.di2it.file_service.application.port.PublishDocumentUploadedPort;
import com.di2it.file_service.application.port.SaveAttachmentPort;
import com.di2it.file_service.config.UploadProperties;
import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.domain.exception.InvalidFileUploadException;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Use case: upload a file to MinIO, persist attachment metadata, and publish ticket.document.uploaded event.
 */
@Service
public class UploadFileUseCase {

    private static final String METADATA_TICKET_ID = "ticket-id";
    private static final String METADATA_TENANT_ID = "tenant-id";

    private final UploadProperties uploadProperties;
    private final ObjectStoragePort objectStoragePort;
    private final SaveAttachmentPort saveAttachmentPort;
    private final PublishDocumentUploadedPort publishDocumentUploadedPort;

    public UploadFileUseCase(
        UploadProperties uploadProperties,
        ObjectStoragePort objectStoragePort,
        SaveAttachmentPort saveAttachmentPort,
        PublishDocumentUploadedPort publishDocumentUploadedPort
    ) {
        this.uploadProperties = uploadProperties;
        this.objectStoragePort = objectStoragePort;
        this.saveAttachmentPort = saveAttachmentPort;
        this.publishDocumentUploadedPort = publishDocumentUploadedPort;
    }

    /**
     * Validates file size and content type, uploads to MinIO, saves metadata, and publishes event.
     *
     * @param command upload command with ticket/tenant/user context and file stream
     * @return the persisted attachment
     * @throws InvalidFileUploadException if size or content type is not allowed
     */
    public Attachment upload(UploadFileCommand command) {
        validateFileSize(command.getFileSize());
        validateContentType(command.getContentType());

        String minioKey = buildMinioKey(
            command.getTenantId(),
            command.getTicketId(),
            command.getFileName()
        );

        Map<String, String> userMetadata = Map.of(
            METADATA_TICKET_ID, command.getTicketId().toString(),
            METADATA_TENANT_ID, command.getTenantId().toString()
        );

        objectStoragePort.putObject(
            minioKey,
            command.getInputStream(),
            command.getFileSize(),
            command.getContentType(),
            userMetadata
        );

        Attachment attachment = buildAttachment(command, minioKey);
        Attachment saved = saveAttachmentPort.save(attachment);

        publishDocumentUploadedEvent(saved);

        return saved;
    }

    private void validateFileSize(long fileSize) {
        if (fileSize <= 0) {
            throw new InvalidFileUploadException("File size must be positive");
        }
        if (fileSize > uploadProperties.getMaxSizeBytes()) {
            throw new InvalidFileUploadException(
                "File size exceeds maximum allowed: " + uploadProperties.getMaxSizeBytes() + " bytes"
            );
        }
    }

    private void validateContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new InvalidFileUploadException("Content type is required");
        }
        String normalized = contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
        boolean allowed = uploadProperties.getAllowedContentTypesList().stream()
            .anyMatch(allowedType -> allowedType.equals(normalized));
        if (!allowed) {
            throw new InvalidFileUploadException(
                "Content type not allowed: " + contentType + ". Allowed: "
                    + uploadProperties.getAllowedContentTypesList()
            );
        }
    }

    private static String buildMinioKey(UUID tenantId, UUID ticketId, String fileName) {
        String safeName = fileName != null && !fileName.isBlank() ? fileName : "file";
        return tenantId + "/" + ticketId + "/" + UUID.randomUUID() + "-" + safeName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static Attachment buildAttachment(UploadFileCommand command, String minioKey) {
        return Attachment.builder()
            .tenantId(command.getTenantId())
            .ticketId(command.getTicketId())
            .uploadedBy(command.getUploadedBy())
            .uploaderRole(command.getUploaderRole())
            .fileName(command.getFileName())
            .minioKey(minioKey)
            .mimeType(normalizeContentType(command.getContentType()))
            .fileSize(command.getFileSize())
            .visibleTo(command.getVisibleToOrDefault())
            .createdAt(LocalDateTime.now())
            .build();
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        return contentType.split(";")[0].trim();
    }

    private void publishDocumentUploadedEvent(Attachment saved) {
        TicketDocumentUploadedEvent event = TicketDocumentUploadedEvent.builder()
            .attachmentId(saved.getId())
            .ticketId(saved.getTicketId())
            .tenantId(saved.getTenantId())
            .uploadedBy(saved.getUploadedBy())
            .fileName(saved.getFileName())
            .mimeType(saved.getMimeType())
            .fileSize(saved.getFileSize())
            .timestamp(Instant.now())
            .build();
        publishDocumentUploadedPort.publish(event);
    }
}
