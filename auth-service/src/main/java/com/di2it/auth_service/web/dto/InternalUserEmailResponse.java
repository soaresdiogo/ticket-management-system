package com.di2it.auth_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Internal API response with user id and email for notification delivery.
 */
@Schema(description = "User email for internal service-to-service lookup")
public record InternalUserEmailResponse(
    @Schema(description = "User ID", example = "11111111-1111-1111-1111-111111111111")
    UUID id,
    @Schema(description = "User email", example = "user@example.com")
    String email
) {
}
