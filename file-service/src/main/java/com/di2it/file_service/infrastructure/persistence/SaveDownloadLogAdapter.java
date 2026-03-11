package com.di2it.file_service.infrastructure.persistence;

import com.di2it.file_service.application.port.SaveDownloadLogPort;
import com.di2it.file_service.domain.entity.DownloadLog;
import com.di2it.file_service.domain.repository.DownloadLogRepository;

import org.springframework.stereotype.Component;

/**
 * JPA adapter for persisting download log entries.
 */
@Component
public class SaveDownloadLogAdapter implements SaveDownloadLogPort {

    private final DownloadLogRepository downloadLogRepository;

    public SaveDownloadLogAdapter(DownloadLogRepository downloadLogRepository) {
        this.downloadLogRepository = downloadLogRepository;
    }

    @Override
    public void save(DownloadLog log) {
        downloadLogRepository.save(log);
    }
}
