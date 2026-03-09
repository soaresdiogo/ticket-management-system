package com.di2it.auth_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Schema(description = "Created user details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserResponse {

    @Schema(description = "User ID")
    private UUID id;
    @Schema(description = "Tenant ID")
    private UUID tenantId;
    @Schema(description = "User email")
    private String email;
    @Schema(description = "Full name")
    private String fullName;
    @Schema(description = "User role")
    private String role;
    @Schema(description = "Whether the user is active")
    private boolean active;
    @Schema(description = "Whether the user must change password on first login")
    private boolean firstAccess;
}
