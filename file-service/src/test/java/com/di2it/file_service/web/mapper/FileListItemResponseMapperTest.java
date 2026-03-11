package com.di2it.file_service.web.mapper;

import com.di2it.file_service.domain.entity.Attachment;
import com.di2it.file_service.web.dto.FileListItemResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FileListItemResponseMapperTest {

    @Test
    @DisplayName("toResponse maps attachment to DTO")
    void toResponse() {
        UUID id = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        Attachment attachment = Attachment.builder()
            .id(id)
            .ticketId(ticketId)
            .fileName("doc.pdf")
            .mimeType("application/pdf")
            .fileSize(100L)
            .createdAt(createdAt)
            .build();

        FileListItemResponse response = FileListItemResponseMapper.toResponse(attachment);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.ticketId()).isEqualTo(ticketId);
        assertThat(response.fileName()).isEqualTo("doc.pdf");
        assertThat(response.mimeType()).isEqualTo("application/pdf");
        assertThat(response.fileSize()).isEqualTo(100L);
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("toResponse returns null for null input")
    void toResponseNull() {
        assertThat(FileListItemResponseMapper.toResponse(null)).isNull();
    }

    @Test
    @DisplayName("toResponseList maps list and returns empty for null")
    void toResponseList() {
        Attachment a = Attachment.builder()
            .id(UUID.randomUUID())
            .ticketId(UUID.randomUUID())
            .fileName("a.pdf")
            .mimeType("application/pdf")
            .fileSize(50L)
            .createdAt(LocalDateTime.now())
            .build();
        List<FileListItemResponse> list = FileListItemResponseMapper.toResponseList(List.of(a));
        assertThat(list).hasSize(1);
        assertThat(list.get(0).fileName()).isEqualTo("a.pdf");
        assertThat(FileListItemResponseMapper.toResponseList(null)).isEmpty();
    }
}
