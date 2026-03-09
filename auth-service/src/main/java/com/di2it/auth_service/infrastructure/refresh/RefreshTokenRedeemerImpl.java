package com.di2it.auth_service.infrastructure.refresh;

import com.di2it.auth_service.application.port.RefreshTokenRedeemer;
import com.di2it.auth_service.domain.entity.RefreshToken;
import com.di2it.auth_service.domain.entity.User;
import com.di2it.auth_service.domain.repository.RefreshTokenRepository;
import com.di2it.auth_service.service.InvalidRefreshTokenException;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Validates a refresh token by hash, ensures it is not revoked and not expired,
 * revokes it (one-time use), and returns the associated user.
 */
@Component
public class RefreshTokenRedeemerImpl implements RefreshTokenRedeemer {

    private static final String INVALID_TOKEN_MESSAGE = "Invalid or expired refresh token.";

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenRedeemerImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public User redeem(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException(INVALID_TOKEN_MESSAGE);
        }

        String tokenHash = TokenHashUtil.sha256Hex(rawToken.trim());
        LocalDateTime now = LocalDateTime.now();

        RefreshToken entity = refreshTokenRepository
            .findByTokenHashAndRevokedFalseAndExpiresAtAfter(tokenHash, now)
            .orElseThrow(() -> new InvalidRefreshTokenException(INVALID_TOKEN_MESSAGE));

        entity.setRevoked(true);
        refreshTokenRepository.save(entity);

        return entity.getUser();
    }
}
