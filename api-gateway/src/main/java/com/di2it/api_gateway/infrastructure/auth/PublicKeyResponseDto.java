package com.di2it.api_gateway.infrastructure.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for auth-service GET /auth/public-key response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublicKeyResponseDto {

    @JsonProperty("publicKey")
    private String publicKey;

    @JsonProperty("keyId")
    private String keyId;

    @JsonProperty("algorithm")
    private String algorithm;
}
