package com.di2it.file_service.domain.exception;

/**
 * Thrown when an attachment is not found or not accessible for the given tenant.
 */
public class AttachmentNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AttachmentNotFoundException(String message) {
        super(message);
    }
}
