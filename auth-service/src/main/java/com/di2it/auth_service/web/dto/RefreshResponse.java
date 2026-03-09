package com.di2it.auth_service.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshResponse {

    public static final String TOKEN_TYPE_BEARER = "Bearer";

    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String refreshToken;
}
