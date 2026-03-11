package com.di2it.file_service.infrastructure.storage;

/**
 * Thrown when an object storage operation (MinIO/S3) fails.
 */
public class ObjectStorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ObjectStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
