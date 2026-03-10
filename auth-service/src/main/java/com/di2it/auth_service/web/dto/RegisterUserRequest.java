package com.di2it.auth_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request to register a new user for a tenant")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {

    @Schema(description = "User email", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @Schema(
        description = "Password (min 8 chars, 1 upper, 1 lower, 1 digit, 1 special)",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100)
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
        message = "Password must be at least 8 characters with 1 upper, 1 lower, 1 number and 1 special"
    )
    private String password;

    @Schema(description = "Full name", example = "Jane Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Full name is required")
    @Size(max = 255)
    private String fullName;

    @Schema(description = "User role", example = "USER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Role is required")
    @Size(max = 50)
    private String role;
}
