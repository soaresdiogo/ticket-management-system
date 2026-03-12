package com.di2it.auth_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Token response after successful MFA verification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyMfaResponse {

    public static final String TOKEN_TYPE_BEARER = "Bearer";

    @Schema(description = "RS256 signed JWT access token")
    private String accessToken;

    @Schema(description = "Token type for Authorization header (e.g. Bearer)")
    private String tokenType;

    @Schema(description = "Access token validity in seconds")
    private long expiresIn;

    @Schema(
        description = "Refresh token is set in HttpOnly cookie (tms_refresh_token). Use POST /auth/refresh."
    )
    private Boolean refreshTokenSet;
}
