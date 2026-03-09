package com.di2it.auth_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "New tokens issued after refresh")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshResponse {

    public static final String TOKEN_TYPE_BEARER = "Bearer";

    @Schema(description = "New JWT access token")
    private String accessToken;
    @Schema(description = "Token type (Bearer)")
    private String tokenType;
    @Schema(description = "Access token validity in seconds")
    private long expiresIn;
    @Schema(description = "New refresh token (rotation)")
    private String refreshToken;
}
