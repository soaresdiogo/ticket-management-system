package com.di2it.notification_service.web.dto;

import com.di2it.notification_service.domain.entity.Notification;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for pushing a notification over WebSocket (minimal fields for client).
 */
public record NotificationPayload(
    UUID id,
    UUID userId,
    String type,
    String title,
    String message,
    UUID referenceId,
    boolean read,
    Instant createdAt
) {
    public static NotificationPayload from(Notification n) {
        return new NotificationPayload(
            n.getId(),
            n.getUserId(),
            n.getType(),
            n.getTitle(),
            n.getMessage(),
            n.getReferenceId(),
            n.isRead(),
            n.getCreatedAt()
        );
    }
}
