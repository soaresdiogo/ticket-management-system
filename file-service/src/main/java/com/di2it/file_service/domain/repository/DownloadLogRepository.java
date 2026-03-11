package com.di2it.file_service.domain.repository;

import com.di2it.file_service.domain.entity.DownloadLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DownloadLogRepository extends JpaRepository<DownloadLog, UUID> {
}
