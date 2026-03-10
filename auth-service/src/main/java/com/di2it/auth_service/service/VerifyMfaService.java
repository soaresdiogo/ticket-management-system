package com.di2it.auth_service.service;

import com.di2it.auth_service.application.AccessTokenClaims;
import com.di2it.auth_service.application.port.JwtTokenIssuer;
import com.di2it.auth_service.application.port.MfaCodeStorage;
import com.di2it.auth_service.application.port.RefreshTokenCreator;
import com.di2it.auth_service.config.JwtKeyProperties;
import com.di2it.auth_service.domain.entity.User;
import com.di2it.auth_service.domain.repository.UserRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.Locale;

/**
 * Use case: validate MFA code from Redis, issue RS256 JWT and optional refresh token.
 */
@Service
@RequiredArgsConstructor
public class VerifyMfaService {

    private static final String INVALID_CODE_MESSAGE = "Invalid or expired verification code.";

    private final MfaCodeStorage mfaCodeStorage;
    private final UserRepository userRepository;
    private final JwtTokenIssuer jwtTokenIssuer;
    private final RefreshTokenCreator refreshTokenCreator;
    private final JwtKeyProperties jwtKeyProperties;

    /**
     * Validates the MFA code for the given email, consumes the code from Redis,
     * and issues an access token (and optionally a refresh token).
     *
     * @param email               user email (normalized to lowercase)
     * @param code                the MFA code from the user
     * @param includeRefreshToken whether to issue a refresh token
     * @return result containing access token, expiry, and optional refresh token
     * @throws InvalidMfaCodeException when the code is missing, expired, or does not match
     */
    public VerifyMfaResult verify(String email, String code, boolean includeRefreshToken) {
        String normalizedEmail = normalizeEmail(email);
        String storedCode = mfaCodeStorage.get(normalizedEmail)
            .orElseThrow(() -> new InvalidMfaCodeException(INVALID_CODE_MESSAGE));

        if (!storedCode.equals(normalizeCode(code))) {
            throw new InvalidMfaCodeException(INVALID_CODE_MESSAGE);
        }

        mfaCodeStorage.remove(normalizedEmail);

        User user = findActiveUser(normalizedEmail);
        AccessTokenClaims claims = buildClaims(user);
        long accessExpiry = jwtKeyProperties.getAccessTokenExpirySeconds();
        String accessToken = jwtTokenIssuer.createAccessToken(claims, accessExpiry);

        String refreshToken = includeRefreshToken
            ? refreshTokenCreator.create(user, jwtKeyProperties.getRefreshTokenExpirySeconds())
            : null;

        return VerifyMfaResult.builder()
            .accessToken(accessToken)
            .expiresInSeconds(accessExpiry)
            .refreshToken(refreshToken)
            .build();
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeCode(String code) {
        return code == null ? "" : code.trim();
    }

    private User findActiveUser(String email) {
        return userRepository.findByEmail(email)
            .filter(User::isActive)
            .orElseThrow(() -> new InvalidMfaCodeException(INVALID_CODE_MESSAGE));
    }

    private static AccessTokenClaims buildClaims(User user) {
        return AccessTokenClaims.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole())
            .tenantId(user.getTenant().getId())
            .build();
    }
}
