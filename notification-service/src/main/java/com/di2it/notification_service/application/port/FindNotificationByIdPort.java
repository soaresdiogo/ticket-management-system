package com.di2it.notification_service.application.port;

import com.di2it.notification_service.domain.entity.Notification;

import java.util.Optional;
import java.util.UUID;

/**
 * Port for finding a notification by id.
 */
@FunctionalInterface
public interface FindNotificationByIdPort {

    Optional<Notification> findById(UUID id);
}
