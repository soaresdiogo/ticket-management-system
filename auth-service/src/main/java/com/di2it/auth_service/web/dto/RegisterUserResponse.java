package com.di2it.auth_service.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserResponse {

    private UUID id;
    private UUID tenantId;
    private String email;
    private String fullName;
    private String role;
    private boolean active;
    private boolean firstAccess;
}
