package com.di2it.notification_service.application.port;

import com.di2it.notification_service.domain.entity.Notification;

/**
 * Port for persisting notifications.
 */
@FunctionalInterface
public interface PersistNotificationPort {

    Notification save(Notification notification);
}
