package com.di2it.notification_service.application.port;

import com.di2it.notification_service.domain.entity.Notification;

/**
 * Port for pushing a notification to real-time clients (e.g. WebSocket).
 */
@FunctionalInterface
public interface PushNotificationPort {

    /**
     * Pushes the notification to subscribed clients.
     *
     * @param notification saved notification to broadcast
     */
    void push(Notification notification);
}
