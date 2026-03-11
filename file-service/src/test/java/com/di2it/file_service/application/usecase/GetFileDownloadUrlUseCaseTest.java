package com.di2it.file_service.application.usecase;

import com.di2it.file_service.application.port.FindAttachmentPort;
import com.di2it.file_service.application.port.ObjectStoragePort;
import com.di2it.file_service.application.port.SaveDownloadLogPort;
import com.di2it.file_service.config.MinioProperties;
import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.domain.exception.AttachmentNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFileDownloadUrlUseCaseTest {

    private static final String PRESIGNED_URL = "https://minio.example.com/bucket/key?X-Amz-Signature=abc";
    private static final int EXPIRY_SECONDS = 900;

    @Mock
    private FindAttachmentPort findAttachmentPort;

    @Mock
    private ObjectStoragePort objectStoragePort;

    @Mock
    private SaveDownloadLogPort saveDownloadLogPort;

    private MinioProperties minioProperties;

    private GetFileDownloadUrlUseCase useCase;

    private UUID attachmentId;
    private UUID tenantId;
    private UUID userId;
    private Attachment attachment;

    @BeforeEach
    void setUp() {
        minioProperties = new MinioProperties();
        minioProperties.setPresignedExpirySeconds(EXPIRY_SECONDS);
        useCase = new GetFileDownloadUrlUseCase(
            findAttachmentPort,
            objectStoragePort,
            saveDownloadLogPort,
            minioProperties
        );
        attachmentId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        attachment = Attachment.builder()
            .id(attachmentId)
            .tenantId(tenantId)
            .ticketId(UUID.randomUUID())
            .uploadedBy(userId)
            .uploaderRole("USER")
            .fileName("doc.pdf")
            .minioKey("tenant/ticket/uuid-doc.pdf")
            .mimeType("application/pdf")
            .fileSize(100L)
            .visibleTo("ALL")
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("getDownloadUrl")
    class GetDownloadUrl {

        @Test
        @DisplayName("returns presigned URL and logs download when attachment found")
        void success() {
            when(findAttachmentPort.findByIdAndTenantId(attachmentId, tenantId)).thenReturn(Optional.of(attachment));
            when(objectStoragePort.getPresignedDownloadUrl(attachment.getMinioKey())).thenReturn(PRESIGNED_URL);

            GetFileDownloadUrlUseCase.FileDownloadResult result = useCase.getDownloadUrl(
                attachmentId,
                tenantId,
                userId,
                Optional.of("192.168.1.1")
            );

            assertThat(result.url()).isEqualTo(PRESIGNED_URL);
            assertThat(result.expiresInSeconds()).isEqualTo(EXPIRY_SECONDS);
            assertThat(result.fileName()).isEqualTo("doc.pdf");

            ArgumentCaptor<com.di2it.file_service.domain.entity.DownloadLog> logCaptor =
                ArgumentCaptor.forClass(com.di2it.file_service.domain.entity.DownloadLog.class);
            verify(saveDownloadLogPort).save(logCaptor.capture());
            assertThat(logCaptor.getValue().getAttachment()).isEqualTo(attachment);
            assertThat(logCaptor.getValue().getDownloadedBy()).isEqualTo(userId);
            assertThat(logCaptor.getValue().getIpAddress()).isEqualTo("192.168.1.1");
        }

        @Test
        @DisplayName("returns presigned URL with empty IP when ipAddress not provided")
        void successWithoutIp() {
            when(findAttachmentPort.findByIdAndTenantId(attachmentId, tenantId)).thenReturn(Optional.of(attachment));
            when(objectStoragePort.getPresignedDownloadUrl(attachment.getMinioKey())).thenReturn(PRESIGNED_URL);

            GetFileDownloadUrlUseCase.FileDownloadResult result = useCase.getDownloadUrl(
                attachmentId,
                tenantId,
                userId,
                Optional.empty()
            );

            assertThat(result.url()).isEqualTo(PRESIGNED_URL);
            ArgumentCaptor<com.di2it.file_service.domain.entity.DownloadLog> logCaptor =
                ArgumentCaptor.forClass(com.di2it.file_service.domain.entity.DownloadLog.class);
            verify(saveDownloadLogPort).save(logCaptor.capture());
            assertThat(logCaptor.getValue().getIpAddress()).isNull();
        }

        @Test
        @DisplayName("throws AttachmentNotFoundException when attachment not found")
        void notFound() {
            when(findAttachmentPort.findByIdAndTenantId(attachmentId, tenantId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.getDownloadUrl(attachmentId, tenantId, userId, Optional.empty()))
                .isInstanceOf(AttachmentNotFoundException.class)
                .hasMessageContaining("Attachment not found");
            verify(objectStoragePort, never()).getPresignedDownloadUrl(any());
            verify(saveDownloadLogPort, never()).save(any());
        }
    }
}
