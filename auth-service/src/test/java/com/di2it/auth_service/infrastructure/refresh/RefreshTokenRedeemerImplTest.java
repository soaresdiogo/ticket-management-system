package com.di2it.auth_service.infrastructure.refresh;

import com.di2it.auth_service.domain.entity.RefreshToken;
import com.di2it.auth_service.domain.entity.Tenant;
import com.di2it.auth_service.domain.entity.User;
import com.di2it.auth_service.domain.repository.RefreshTokenRepository;
import com.di2it.auth_service.service.InvalidRefreshTokenException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRedeemerImplTest {

    private static final String VALID_RAW_TOKEN = "valid-opaque-token";

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenRedeemerImpl refreshTokenRedeemer;

    private User user;
    private RefreshToken refreshTokenEntity;

    @BeforeEach
    void setUp() {
        Tenant tenant = Tenant.builder()
            .id(UUID.randomUUID())
            .name("Acme")
            .email("admin@acme.com")
            .active(true)
            .build();
        user = User.builder()
            .id(UUID.randomUUID())
            .tenant(tenant)
            .email("user@example.com")
            .passwordHash("hash")
            .fullName("John Doe")
            .role("CLIENT")
            .active(true)
            .build();

        String tokenHash = TokenHashUtil.sha256Hex(VALID_RAW_TOKEN);
        refreshTokenEntity = RefreshToken.builder()
            .id(UUID.randomUUID())
            .user(user)
            .tokenHash(tokenHash)
            .expiresAt(LocalDateTime.now().plusDays(7))
            .revoked(false)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("redeem")
    class Redeem {

        @Test
        @DisplayName("returns user and revokes token when valid")
        void success() {
            when(refreshTokenRepository.findByTokenHashAndRevokedFalseAndExpiresAtAfter(
                any(), any(LocalDateTime.class)
            )).thenReturn(Optional.of(refreshTokenEntity));

            User result = refreshTokenRedeemer.redeem(VALID_RAW_TOKEN);

            assertThat(result).isSameAs(user);
            assertThat(refreshTokenEntity.isRevoked()).isTrue();
            ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
            verify(refreshTokenRepository).findByTokenHashAndRevokedFalseAndExpiresAtAfter(
                hashCaptor.capture(), any(LocalDateTime.class)
            );
            assertThat(hashCaptor.getValue()).isEqualTo(TokenHashUtil.sha256Hex(VALID_RAW_TOKEN));
            verify(refreshTokenRepository).save(refreshTokenEntity);
        }

        @Test
        @DisplayName("trims raw token before hashing")
        void trimsToken() {
            when(refreshTokenRepository.findByTokenHashAndRevokedFalseAndExpiresAtAfter(
                eq(TokenHashUtil.sha256Hex(VALID_RAW_TOKEN)), any(LocalDateTime.class)
            )).thenReturn(Optional.of(refreshTokenEntity));

            User result = refreshTokenRedeemer.redeem("  " + VALID_RAW_TOKEN + "  ");

            assertThat(result).isSameAs(user);
        }

        @Test
        @DisplayName("throws when token is null")
        void nullToken() {
            assertThatThrownBy(() -> refreshTokenRedeemer.redeem(null))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("Invalid or expired refresh token");
            verify(refreshTokenRepository, never()).findByTokenHashAndRevokedFalseAndExpiresAtAfter(any(), any());
        }

        @Test
        @DisplayName("throws when token is blank")
        void blankToken() {
            assertThatThrownBy(() -> refreshTokenRedeemer.redeem("   "))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("Invalid or expired refresh token");
            verify(refreshTokenRepository, never()).findByTokenHashAndRevokedFalseAndExpiresAtAfter(any(), any());
        }

        @Test
        @DisplayName("throws when token not found or expired")
        void tokenNotFound() {
            when(refreshTokenRepository.findByTokenHashAndRevokedFalseAndExpiresAtAfter(
                any(), any(LocalDateTime.class)
            )).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenRedeemer.redeem(VALID_RAW_TOKEN))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("Invalid or expired refresh token");
            verify(refreshTokenRepository, never()).save(any());
        }
    }
}
