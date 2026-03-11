package com.di2it.file_service.domain.exception;

/**
 * Thrown when file upload validation fails (e.g. size exceeded, disallowed type).
 */
public class InvalidFileUploadException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidFileUploadException(String message) {
        super(message);
    }

    public InvalidFileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
