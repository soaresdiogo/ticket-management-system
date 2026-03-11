package com.di2it.file_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * File upload constraints: max size and allowed MIME types.
 * Bound from file-service.upload.* (e.g. file-service.upload.max-size-bytes).
 */
@ConfigurationProperties(prefix = "file-service.upload")
@Getter
@Setter
@Validated
public class UploadProperties {

    private static final String DEFAULT_ALLOWED_TYPES =
        "application/pdf,image/jpeg,image/jpg,image/png,"
            + "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * Maximum file size in bytes (e.g. 10485760 = 10 MB).
     */
    @NotNull
    @Positive
    private Long maxSizeBytes = 10_485_760L;

    /**
     * Comma-separated allowed MIME types (e.g. application/pdf, image/jpeg).
     */
    @NotBlank
    private String allowedContentTypes = DEFAULT_ALLOWED_TYPES;

    /**
     * Returns allowed content types as a list (lowercase, trimmed).
     */
    public List<String> getAllowedContentTypesList() {
        return Arrays.stream(allowedContentTypes.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(String::toLowerCase)
            .collect(Collectors.toList());
    }
}
