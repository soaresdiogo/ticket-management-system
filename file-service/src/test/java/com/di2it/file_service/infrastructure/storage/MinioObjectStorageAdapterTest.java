package com.di2it.file_service.infrastructure.storage;

import com.di2it.file_service.config.MinioProperties;

import io.minio.MinioClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioObjectStorageAdapterTest {

    private static final String BUCKET = "test-bucket";
    private static final String KEY = "tenant/123/file.pdf";
    private static final int PRESIGNED_EXPIRY = 900;

    @Mock
    private MinioClient minioClient;

    private MinioProperties minioProperties;
    private MinioObjectStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        minioProperties = new MinioProperties();
        minioProperties.setBucket(BUCKET);
        minioProperties.setPresignedExpirySeconds(PRESIGNED_EXPIRY);
        adapter = new MinioObjectStorageAdapter(minioClient, minioProperties);
    }

    @Test
    @DisplayName("ensureBucketExists throws ObjectStorageException when MinIO client fails")
    void ensureBucketExistsPropagatesException() throws Exception {
        doThrow(new RuntimeException("Connection refused")).when(minioClient).bucketExists(any());

        assertThatThrownBy(() -> adapter.ensureBucketExists())
            .isInstanceOf(ObjectStorageException.class)
            .hasMessageContaining("Failed to ensure bucket exists")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("putObject throws ObjectStorageException when MinIO client fails")
    void putObjectPropagatesException() throws Exception {
        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        doThrow(new RuntimeException("Upload failed")).when(minioClient).putObject(any());

        assertThatThrownBy(() -> adapter.putObject(KEY, stream, 3L, "application/pdf", Map.of()))
            .isInstanceOf(ObjectStorageException.class)
            .hasMessageContaining("Failed to put object")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("getPresignedDownloadUrl throws ObjectStorageException when MinIO client fails")
    void getPresignedDownloadUrlPropagatesException() throws Exception {
        doThrow(new RuntimeException("Presign failed")).when(minioClient).getPresignedObjectUrl(any());

        assertThatThrownBy(() -> adapter.getPresignedDownloadUrl(KEY))
            .isInstanceOf(ObjectStorageException.class)
            .hasMessageContaining("Failed to get presigned URL")
            .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("ensureBucketExists does not call makeBucket when bucket already exists")
    void ensureBucketExistsDoesNotCreateBucketWhenExists() throws Exception {
        when(minioClient.bucketExists(any())).thenReturn(true);

        adapter.ensureBucketExists();

        verify(minioClient).bucketExists(any());
        verify(minioClient, org.mockito.Mockito.never()).makeBucket(any());
    }

    @Test
    @DisplayName("ensureBucketExists calls makeBucket when bucket does not exist and propagates failure")
    void ensureBucketExistsCallsMakeBucketAndPropagatesException() throws Exception {
        when(minioClient.bucketExists(any())).thenReturn(false);
        doThrow(new RuntimeException("Make bucket failed")).when(minioClient).makeBucket(any());

        assertThatThrownBy(() -> adapter.ensureBucketExists())
            .isInstanceOf(ObjectStorageException.class)
            .hasMessageContaining("Failed to ensure bucket exists");

        verify(minioClient).bucketExists(any());
        verify(minioClient).makeBucket(any());
    }
}
