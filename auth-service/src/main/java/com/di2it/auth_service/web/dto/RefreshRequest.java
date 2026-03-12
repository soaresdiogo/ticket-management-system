package com.di2it.auth_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(
    description = "Optional body when refresh token is sent via HttpOnly cookie. If cookie is set, body omitted.")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {

    @Schema(description = "Refresh token (optional when sent in cookie tms_refresh_token)")
    @Size(max = 512)
    private String refreshToken;
}
