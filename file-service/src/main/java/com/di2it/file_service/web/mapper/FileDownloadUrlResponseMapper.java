package com.di2it.file_service.web.mapper;

import com.di2it.file_service.application.usecase.GetFileDownloadUrlUseCase;
import com.di2it.file_service.web.dto.FileDownloadUrlResponse;

/**
 * Maps use case download result to API response DTO.
 */
public final class FileDownloadUrlResponseMapper {

    private FileDownloadUrlResponseMapper() {
    }

    public static FileDownloadUrlResponse toResponse(GetFileDownloadUrlUseCase.FileDownloadResult result) {
        if (result == null) {
            return null;
        }
        return new FileDownloadUrlResponse(
            result.url(),
            result.expiresInSeconds(),
            result.fileName()
        );
    }
}
