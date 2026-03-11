package com.di2it.notification_service.domain.repository;

import com.di2it.notification_service.domain.entity.EmailLog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for EmailLog entity.
 */
public interface EmailLogRepository extends JpaRepository<EmailLog, UUID> {
}
