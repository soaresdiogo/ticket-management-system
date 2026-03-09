package com.di2it.auth_service.infrastructure.jwt;

import com.di2it.auth_service.application.AccessTokenClaims;
import com.di2it.auth_service.application.port.JwtTokenIssuer;
import com.di2it.auth_service.security.JwtKeyService;

import io.jsonwebtoken.Jwts;

import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Issues RS256 JWTs using the auth-service private key.
 */
@Component
public class JwtTokenIssuerImpl implements JwtTokenIssuer {

    private final JwtKeyService jwtKeyService;

    public JwtTokenIssuerImpl(JwtKeyService jwtKeyService) {
        this.jwtKeyService = jwtKeyService;
    }

    @Override
    public String createAccessToken(AccessTokenClaims claims, long expirySeconds) {
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiration = new Date(now + expirySeconds * 1000L);

        return Jwts.builder()
            .header().keyId(jwtKeyService.getKeyId()).and()
            .subject(claims.getUserId().toString())
            .claim("email", claims.getEmail())
            .claim("role", claims.getRole())
            .claim("tenantId", claims.getTenantId().toString())
            .issuedAt(issuedAt)
            .expiration(expiration)
            .signWith(jwtKeyService.getPrivateKey(), Jwts.SIG.RS256)
            .compact();
    }
}
