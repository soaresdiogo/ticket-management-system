package com.di2it.auth_service.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyResponse {

    /**
     * Public key in PEM format (X.509) for JWT validation.
     */
    private String publicKey;

    /**
     * Key identifier (e.g. for key rotation).
     */
    private String keyId;

    /**
     * JWT signing algorithm (e.g. RS256).
     */
    private String algorithm;
}
