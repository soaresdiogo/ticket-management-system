package com.di2it.file_service.application.port;

import java.io.InputStream;
import java.util.Map;

/**
 * Port for object storage (MinIO/S3). Keeps file service independent of storage implementation.
 */
public interface ObjectStoragePort {

    /**
     * Ensures the bucket exists; creates it if missing.
     */
    void ensureBucketExists();

    /**
     * Uploads object content under the given key.
     *
     * @param key    object key (path in bucket)
     * @param stream content stream
     * @param size   content length
     * @param contentType MIME type
     * @param userMetadata optional metadata
     */
    void putObject(String key, InputStream stream, long size, String contentType, Map<String, String> userMetadata);

    /**
     * Returns a presigned GET URL for the object, valid for the configured TTL.
     *
     * @param key object key
     * @return presigned URL string
     */
    String getPresignedDownloadUrl(String key);
}
