package com.di2it.file_service.config;

import io.minio.MinioClient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MinioConfigTest {

    @Autowired(required = false)
    private MinioClient minioClient;

    @Autowired(required = false)
    private MinioProperties minioProperties;

    @Test
    @DisplayName("MinioClient bean is created when MinIO is enabled")
    void minioClientBeanExists() {
        assertThat(minioClient).isNotNull();
    }

    @Test
    @DisplayName("MinioProperties is bound from configuration")
    void minioPropertiesBound() {
        assertThat(minioProperties).isNotNull();
        assertThat(minioProperties.getEndpoint()).isNotBlank();
        assertThat(minioProperties.getBucket()).isNotBlank();
        assertThat(minioProperties.getPresignedExpirySeconds()).isPositive();
    }
}
