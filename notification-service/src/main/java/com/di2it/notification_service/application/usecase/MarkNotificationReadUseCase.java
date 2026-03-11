package com.di2it.notification_service.application.usecase;

import com.di2it.notification_service.application.port.FindNotificationByIdPort;
import com.di2it.notification_service.application.port.PersistNotificationPort;
import com.di2it.notification_service.domain.entity.Notification;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case: mark a notification as read. Only the notification owner (userId) may mark it.
 */
@Service
public class MarkNotificationReadUseCase {

    private final FindNotificationByIdPort findNotificationByIdPort;
    private final PersistNotificationPort persistNotificationPort;

    public MarkNotificationReadUseCase(
        FindNotificationByIdPort findNotificationByIdPort,
        PersistNotificationPort persistNotificationPort
    ) {
        this.findNotificationByIdPort = findNotificationByIdPort;
        this.persistNotificationPort = persistNotificationPort;
    }

    /**
     * Marks the notification as read if it exists and belongs to the given user.
     *
     * @param notificationId notification id
     * @param userId         current user (must match notification.userId)
     * @return updated notification if found and owned by user, empty otherwise
     */
    public Optional<Notification> markRead(UUID notificationId, UUID userId) {
        return findNotificationByIdPort.findById(notificationId)
            .filter(n -> userId.equals(n.getUserId()))
            .map(this::markAsRead)
            .map(persistNotificationPort::save);
    }

    private Notification markAsRead(Notification n) {
        n.setRead(true);
        n.setReadAt(Instant.now());
        return n;
    }
}
