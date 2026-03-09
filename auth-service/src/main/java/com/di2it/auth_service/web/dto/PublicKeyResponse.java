package com.di2it.auth_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "JWT public key for validation (RS256)")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyResponse {

    @Schema(description = "Public key in PEM format (X.509) for JWT validation")
    private String publicKey;

    @Schema(description = "Key identifier (e.g. for key rotation)")
    private String keyId;

    @Schema(description = "JWT signing algorithm (e.g. RS256)")
    private String algorithm;
}
