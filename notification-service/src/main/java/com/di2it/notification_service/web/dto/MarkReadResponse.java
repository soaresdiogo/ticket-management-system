package com.di2it.notification_service.web.dto;

import com.di2it.notification_service.domain.entity.Notification;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response for PATCH /notifications/{id}/read.
 */
@Schema(description = "Notification after marking as read")
public record MarkReadResponse(
    @Schema(description = "Notification ID") UUID id,
    @Schema(description = "User ID (owner)") UUID userId,
    @Schema(description = "Notification type") String type,
    @Schema(description = "Title") String title,
    @Schema(description = "Message") String message,
    @Schema(description = "Reference ID (e.g. ticket id)") UUID referenceId,
    @Schema(description = "Whether the notification has been read") boolean read,
    @Schema(description = "When the notification was read") Instant readAt,
    @Schema(description = "When the notification was created") Instant createdAt
) {
    public static MarkReadResponse from(Notification n) {
        return new MarkReadResponse(
            n.getId(),
            n.getUserId(),
            n.getType(),
            n.getTitle(),
            n.getMessage(),
            n.getReferenceId(),
            n.isRead(),
            n.getReadAt(),
            n.getCreatedAt()
        );
    }
}
