package com.di2it.notification_service.web;

import com.di2it.notification_service.application.usecase.MarkNotificationReadUseCase;
import com.di2it.notification_service.web.dto.MarkReadResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for notification operations. Expects X-User-Id from the API Gateway.
 */
@Tag(name = "Notifications", description = "Mark notifications as read")
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private static final String SECURITY_SCHEME_BEARER_JWT = "bearer-jwt";

    private final MarkNotificationReadUseCase markNotificationReadUseCase;

    public NotificationController(MarkNotificationReadUseCase markNotificationReadUseCase) {
        this.markNotificationReadUseCase = markNotificationReadUseCase;
    }

    @Operation(
        summary = "Mark notification as read",
        description = "Marks the notification as read. Only the notification owner (X-User-Id) can mark it.")
    @ApiResponse(responseCode = "200", description = "Notification marked as read")
    @ApiResponse(responseCode = "404", description = "Notification not found or not owned by user")
    @SecurityRequirement(name = SECURITY_SCHEME_BEARER_JWT)
    @PatchMapping("/{id}/read")
    public ResponseEntity<MarkReadResponse> markAsRead(
        @Parameter(description = "Notification ID") @PathVariable UUID id,
        @RequestHeader(name = WebConstants.HEADER_USER_ID, required = false) UUID userId
    ) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return markNotificationReadUseCase.markRead(id, userId)
            .map(MarkReadResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
