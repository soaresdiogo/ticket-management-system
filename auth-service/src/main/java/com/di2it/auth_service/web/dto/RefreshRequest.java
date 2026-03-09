package com.di2it.auth_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request to refresh access token using a refresh token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {

    @Schema(description = "Refresh token from verify-mfa or previous refresh", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Refresh token is required")
    @Size(max = 512)
    private String refreshToken;
}
