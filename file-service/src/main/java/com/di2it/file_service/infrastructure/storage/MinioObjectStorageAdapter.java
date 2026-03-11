package com.di2it.file_service.infrastructure.storage;

import com.di2it.file_service.application.port.ObjectStoragePort;
import com.di2it.file_service.config.MinioProperties;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MinIO implementation of ObjectStoragePort. Delegates to MinioClient for bucket and object operations.
 */
@Component
public class MinioObjectStorageAdapter implements ObjectStoragePort {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public MinioObjectStorageAdapter(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(properties.getBucket()).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
            }
        } catch (Exception e) {
            throw new ObjectStorageException("Failed to ensure bucket exists: " + properties.getBucket(), e);
        }
    }

    @Override
    public void putObject(
        String key,
        InputStream stream,
        long size,
        String contentType,
        Map<String, String> userMetadata
    ) {
        try {
            var argsBuilder = PutObjectArgs.builder()
                .bucket(properties.getBucket())
                .object(key)
                .stream(stream, size, -1)
                .contentType(contentType != null ? contentType : "application/octet-stream");
            if (userMetadata != null && !userMetadata.isEmpty()) {
                argsBuilder.userMetadata(userMetadata);
            }
            minioClient.putObject(argsBuilder.build());
        } catch (Exception e) {
            throw new ObjectStorageException("Failed to put object: " + key, e);
        }
    }

    @Override
    public String getPresignedDownloadUrl(String key) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(properties.getBucket())
                    .object(key)
                    .expiry(properties.getPresignedExpirySeconds(), TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            throw new ObjectStorageException("Failed to get presigned URL for: " + key, e);
        }
    }
}
