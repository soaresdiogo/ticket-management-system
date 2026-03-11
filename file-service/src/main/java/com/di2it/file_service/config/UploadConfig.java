package com.di2it.file_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables upload-related configuration properties (max size, allowed types).
 */
@Configuration
@EnableConfigurationProperties(UploadProperties.class)
public class UploadConfig {
}
