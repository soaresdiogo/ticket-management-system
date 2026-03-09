package com.di2it.auth_service.config;

import com.di2it.auth_service.security.JwtKeyService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.interfaces.RSAPublicKey;

/**
 * Configures JWT decoder for validating access tokens (RS256) on protected endpoints.
 */
@Configuration
public class JwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder(JwtKeyService jwtKeyService) {
        RSAPublicKey publicKey = (RSAPublicKey) jwtKeyService.getPublicKey();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
