package com.di2it.auth_service.application;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Value object holding claims to be encoded in the access JWT (RS256).
 */
@Value
@Builder
public class AccessTokenClaims {

    UUID userId;
    String email;
    String role;
    UUID tenantId;
}
