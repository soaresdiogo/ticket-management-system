package com.di2it.file_service.web;

import com.di2it.file_service.domain.exception.InvalidFileUploadException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Handles domain exceptions and maps them to HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFileUploadException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFileUpload(InvalidFileUploadException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("message", ex.getMessage()));
    }
}
