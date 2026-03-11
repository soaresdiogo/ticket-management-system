package com.di2it.notification_service.domain.repository;

import com.di2it.notification_service.domain.entity.Notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for Notification entity.
 */
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
