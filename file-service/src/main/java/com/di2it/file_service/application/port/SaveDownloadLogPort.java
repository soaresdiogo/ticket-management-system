package com.di2it.file_service.application.port;

import com.di2it.file_service.domain.entity.DownloadLog;

/**
 * Port for persisting download log entries (audit trail).
 */
@FunctionalInterface
public interface SaveDownloadLogPort {

    /**
     * Saves a download log entry.
     *
     * @param log the log to save
     */
    void save(DownloadLog log);
}
