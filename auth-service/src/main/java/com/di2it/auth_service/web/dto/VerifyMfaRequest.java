package com.di2it.auth_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "MFA verification request with code from email")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyMfaRequest {

    @Schema(description = "User email", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @Schema(description = "MFA code received by email", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Verification code is required")
    @Size(min = 4, max = 10, message = "Code must be between 4 and 10 characters")
    private String code;

    @Schema(description = "Whether to include a refresh token in the response; defaults to true when omitted")
    private Boolean includeRefreshToken;
}
