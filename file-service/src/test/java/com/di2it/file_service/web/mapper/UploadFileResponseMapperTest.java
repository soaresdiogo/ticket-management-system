package com.di2it.file_service.web.mapper;

import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.web.dto.UploadFileResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UploadFileResponseMapperTest {

    @Test
    @DisplayName("maps attachment entity to UploadFileResponse")
    void toResponse_mapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        Attachment attachment = Attachment.builder()
            .id(id)
            .tenantId(UUID.randomUUID())
            .ticketId(ticketId)
            .uploadedBy(UUID.randomUUID())
            .uploaderRole("USER")
            .fileName("invoice.pdf")
            .minioKey("tenant/ticket/uuid-invoice.pdf")
            .mimeType("application/pdf")
            .fileSize(2048L)
            .visibleTo("ALL")
            .createdAt(createdAt)
            .build();

        UploadFileResponse response = UploadFileResponseMapper.toResponse(attachment);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.ticketId()).isEqualTo(ticketId);
        assertThat(response.fileName()).isEqualTo("invoice.pdf");
        assertThat(response.mimeType()).isEqualTo("application/pdf");
        assertThat(response.fileSize()).isEqualTo(2048L);
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("returns null when attachment is null")
    void toResponse_returnsNullWhenAttachmentNull() {
        assertThat(UploadFileResponseMapper.toResponse(null)).isNull();
    }
}
