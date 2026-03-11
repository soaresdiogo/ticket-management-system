package com.di2it.file_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * MinIO connection settings. Bound from minio.* (e.g. MINIO_ENDPOINT, MINIO_ACCESS_KEY).
 */
@ConfigurationProperties(prefix = "minio")
@Getter
@Setter
@Validated
public class MinioProperties {

    /**
     * MinIO server endpoint (e.g. http://localhost:9000).
     */
    @NotBlank(message = "minio.endpoint is required")
    private String endpoint = "http://localhost:9000";

    /**
     * Access key (e.g. MINIO_ROOT_USER).
     */
    @NotBlank(message = "minio.access-key is required")
    private String accessKey = "tms";

    /**
     * Secret key (e.g. MINIO_ROOT_PASSWORD).
     */
    @NotBlank(message = "minio.secret-key is required")
    private String secretKey = "tms12345";

    /**
     * Bucket name for file storage.
     */
    @NotBlank(message = "minio.bucket is required")
    private String bucket = "tms-files";

    /**
     * Presigned URL validity in seconds (e.g. 900 = 15 min).
     */
    @NotNull
    @Positive
    private Integer presignedExpirySeconds = 900;
}
