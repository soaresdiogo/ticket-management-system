package com.di2it.auth_service.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyMfaRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Verification code is required")
    @Size(min = 4, max = 10, message = "Code must be between 4 and 10 characters")
    private String code;

    /**
     * Whether to include a refresh token in the response. Defaults to true when omitted.
     */
    private Boolean includeRefreshToken;
}
