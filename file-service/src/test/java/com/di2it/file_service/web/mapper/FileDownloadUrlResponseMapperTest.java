package com.di2it.file_service.web.mapper;

import com.di2it.file_service.application.usecase.GetFileDownloadUrlUseCase;
import com.di2it.file_service.web.dto.FileDownloadUrlResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileDownloadUrlResponseMapperTest {

    @Test
    @DisplayName("toResponse maps result to DTO")
    void toResponse() {
        GetFileDownloadUrlUseCase.FileDownloadResult result =
            new GetFileDownloadUrlUseCase.FileDownloadResult("https://example.com/presigned", 900, "file.pdf");

        FileDownloadUrlResponse response = FileDownloadUrlResponseMapper.toResponse(result);

        assertThat(response).isNotNull();
        assertThat(response.url()).isEqualTo("https://example.com/presigned");
        assertThat(response.expiresInSeconds()).isEqualTo(900);
        assertThat(response.fileName()).isEqualTo("file.pdf");
    }

    @Test
    @DisplayName("toResponse returns null for null input")
    void toResponseNull() {
        assertThat(FileDownloadUrlResponseMapper.toResponse(null)).isNull();
    }
}
