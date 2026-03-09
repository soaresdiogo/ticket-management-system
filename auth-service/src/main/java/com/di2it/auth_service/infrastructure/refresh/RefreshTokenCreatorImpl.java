package com.di2it.auth_service.infrastructure.refresh;

import com.di2it.auth_service.application.port.RefreshTokenCreator;
import com.di2it.auth_service.domain.entity.RefreshToken;
import com.di2it.auth_service.domain.entity.User;
import com.di2it.auth_service.domain.repository.RefreshTokenRepository;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Creates a refresh token, hashes it with SHA-256, and persists it.
 * Returns the raw token only once to the client.
 */
@Component
public class RefreshTokenCreatorImpl implements RefreshTokenCreator {

    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCreatorImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public String create(User user, long expirySeconds) {
        String rawToken = generateSecureToken();
        String tokenHash = TokenHashUtil.sha256Hex(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirySeconds);

        RefreshToken entity = RefreshToken.builder()
            .user(user)
            .tokenHash(tokenHash)
            .expiresAt(expiresAt)
            .revoked(false)
            .createdAt(LocalDateTime.now())
            .build();
        refreshTokenRepository.save(entity);

        return rawToken;
    }

    private static String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
