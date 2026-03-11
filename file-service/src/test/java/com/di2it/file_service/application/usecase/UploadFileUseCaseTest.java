package com.di2it.file_service.application.usecase;

import com.di2it.file_service.application.port.ObjectStoragePort;
import com.di2it.file_service.application.port.PublishDocumentUploadedPort;
import com.di2it.file_service.application.port.SaveAttachmentPort;
import com.di2it.file_service.config.UploadProperties;
import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.domain.exception.InvalidFileUploadException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadFileUseCaseTest {

    private static final long MAX_SIZE = 1024L;
    private static final String ALLOWED_TYPES = "application/pdf,image/jpeg,image/png";

    @Mock
    private ObjectStoragePort objectStoragePort;

    @Mock
    private SaveAttachmentPort saveAttachmentPort;

    @Mock
    private PublishDocumentUploadedPort publishDocumentUploadedPort;

    private UploadProperties uploadProperties;

    @InjectMocks
    private UploadFileUseCase uploadFileUseCase;

    private UUID ticketId;
    private UUID tenantId;
    private UUID uploadedBy;

    @BeforeEach
    void setUp() {
        uploadProperties = new UploadProperties();
        uploadProperties.setMaxSizeBytes(MAX_SIZE);
        uploadProperties.setAllowedContentTypes(ALLOWED_TYPES);
        uploadFileUseCase = new UploadFileUseCase(
            uploadProperties,
            objectStoragePort,
            saveAttachmentPort,
            publishDocumentUploadedPort
        );
        ticketId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        uploadedBy = UUID.randomUUID();
    }

    private UploadFileCommand validCommand(String contentType, long size) {
        InputStream stream = new ByteArrayInputStream(new byte[(int) size]);
        return UploadFileCommand.builder()
            .ticketId(ticketId)
            .tenantId(tenantId)
            .uploadedBy(uploadedBy)
            .uploaderRole("USER")
            .fileName("doc.pdf")
            .contentType(contentType)
            .fileSize(size)
            .inputStream(stream)
            .visibleTo("ALL")
            .build();
    }

    @Nested
    @DisplayName("upload")
    class Upload {

        @Test
        @DisplayName("uploads to storage, saves attachment and publishes event when valid")
        void successFlow() {
            UploadFileCommand command = validCommand("application/pdf", 100L);
            Attachment saved = Attachment.builder()
                .id(UUID.randomUUID())
                .ticketId(ticketId)
                .tenantId(tenantId)
                .uploadedBy(uploadedBy)
                .uploaderRole("USER")
                .fileName("doc.pdf")
                .minioKey(tenantId + "/" + ticketId + "/key")
                .mimeType("application/pdf")
                .fileSize(100L)
                .visibleTo("ALL")
                .createdAt(LocalDateTime.now())
                .build();
            when(saveAttachmentPort.save(any(Attachment.class))).thenReturn(saved);

            Attachment result = uploadFileUseCase.upload(command);

            assertThat(result).isSameAs(saved);
            verify(objectStoragePort).putObject(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(InputStream.class),
                eq(100L),
                eq("application/pdf"),
                org.mockito.ArgumentMatchers.any(Map.class)
            );
            verify(saveAttachmentPort).save(any(Attachment.class));
            ArgumentCaptor<com.di2it.file_service.application.event.TicketDocumentUploadedEvent> eventCaptor =
                ArgumentCaptor.forClass(com.di2it.file_service.application.event.TicketDocumentUploadedEvent.class);
            verify(publishDocumentUploadedPort).publish(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getAttachmentId()).isEqualTo(saved.getId());
            assertThat(eventCaptor.getValue().getTicketId()).isEqualTo(ticketId);
            assertThat(eventCaptor.getValue().getFileName()).isEqualTo("doc.pdf");
        }

        @Test
        @DisplayName("throws when file size exceeds max")
        void throwsWhenFileSizeExceedsMax() {
            UploadFileCommand command = validCommand("application/pdf", MAX_SIZE + 1);

            assertThatThrownBy(() -> uploadFileUseCase.upload(command))
                .isInstanceOf(InvalidFileUploadException.class)
                .hasMessageContaining("exceeds maximum");
            verify(objectStoragePort, never()).putObject(any(), any(), any(Long.class), any(), any());
            verify(saveAttachmentPort, never()).save(any());
            verify(publishDocumentUploadedPort, never()).publish(any());
        }

        @Test
        @DisplayName("throws when file size is zero")
        void throwsWhenFileSizeZero() {
            UploadFileCommand command = validCommand("application/pdf", 0);

            assertThatThrownBy(() -> uploadFileUseCase.upload(command))
                .isInstanceOf(InvalidFileUploadException.class)
                .hasMessageContaining("positive");
            verify(objectStoragePort, never()).putObject(any(), any(), any(Long.class), any(), any());
        }

        @Test
        @DisplayName("throws when content type is not allowed")
        void throwsWhenContentTypeNotAllowed() {
            UploadFileCommand command = validCommand("application/zip", 100L);

            assertThatThrownBy(() -> uploadFileUseCase.upload(command))
                .isInstanceOf(InvalidFileUploadException.class)
                .hasMessageContaining("Content type not allowed");
            verify(objectStoragePort, never()).putObject(any(), any(), any(Long.class), any(), any());
        }

        @Test
        @DisplayName("throws when content type is null")
        void throwsWhenContentTypeNull() {
            UploadFileCommand command = UploadFileCommand.builder()
                .ticketId(ticketId)
                .tenantId(tenantId)
                .uploadedBy(uploadedBy)
                .uploaderRole("USER")
                .fileName("x.pdf")
                .contentType(null)
                .fileSize(100L)
                .inputStream(new ByteArrayInputStream(new byte[100]))
                .build();

            assertThatThrownBy(() -> uploadFileUseCase.upload(command))
                .isInstanceOf(InvalidFileUploadException.class)
                .hasMessageContaining("Content type");
            verify(objectStoragePort, never()).putObject(any(), any(), any(Long.class), any(), any());
        }

        @Test
        @DisplayName("accepts image/jpeg as allowed type")
        void acceptsImageJpeg() {
            UploadFileCommand command = validCommand("image/jpeg", 50L);
            Attachment saved = Attachment.builder()
                .id(UUID.randomUUID())
                .ticketId(ticketId)
                .tenantId(tenantId)
                .uploadedBy(uploadedBy)
                .uploaderRole("USER")
                .fileName("doc.pdf")
                .minioKey("key")
                .mimeType("image/jpeg")
                .fileSize(50L)
                .visibleTo("ALL")
                .createdAt(LocalDateTime.now())
                .build();
            when(saveAttachmentPort.save(any(Attachment.class))).thenReturn(saved);

            Attachment result = uploadFileUseCase.upload(command);

            assertThat(result).isNotNull();
            verify(objectStoragePort).putObject(any(), any(), eq(50L), eq("image/jpeg"), any());
        }
    }
}
