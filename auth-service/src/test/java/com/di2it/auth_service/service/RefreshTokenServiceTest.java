package com.di2it.auth_service.service;

import com.di2it.auth_service.application.AccessTokenClaims;
import com.di2it.auth_service.application.port.JwtTokenIssuer;
import com.di2it.auth_service.application.port.RefreshTokenCreator;
import com.di2it.auth_service.application.port.RefreshTokenRedeemer;
import com.di2it.auth_service.config.JwtKeyProperties;
import com.di2it.auth_service.domain.entity.Tenant;
import com.di2it.auth_service.domain.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final String RAW_REFRESH_TOKEN = "opaque.refresh.token";
    private static final long ACCESS_EXPIRY = 900L;
    private static final long REFRESH_EXPIRY = 604800L;

    @Mock
    private RefreshTokenRedeemer refreshTokenRedeemer;

    @Mock
    private JwtTokenIssuer jwtTokenIssuer;

    @Mock
    private RefreshTokenCreator refreshTokenCreator;

    @Mock
    private JwtKeyProperties jwtKeyProperties;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

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

        lenient().when(jwtKeyProperties.getAccessTokenExpirySeconds()).thenReturn(ACCESS_EXPIRY);
        lenient().when(jwtKeyProperties.getRefreshTokenExpirySeconds()).thenReturn(REFRESH_EXPIRY);
    }

    @Nested
    @DisplayName("refresh")
    class Refresh {

        @Test
        @DisplayName("returns new access and refresh token when token is valid")
        void success() {
            when(refreshTokenRedeemer.redeem(RAW_REFRESH_TOKEN)).thenReturn(user);
            when(jwtTokenIssuer.createAccessToken(any(AccessTokenClaims.class), eq(ACCESS_EXPIRY)))
                .thenReturn("new.jwt.token");
            when(refreshTokenCreator.create(eq(user), eq(REFRESH_EXPIRY))).thenReturn("new.refresh.token");

            RefreshResult result = refreshTokenService.refresh(RAW_REFRESH_TOKEN);

            assertThat(result.getAccessToken()).isEqualTo("new.jwt.token");
            assertThat(result.getExpiresInSeconds()).isEqualTo(ACCESS_EXPIRY);
            assertThat(result.getRefreshToken()).isEqualTo("new.refresh.token");

            ArgumentCaptor<AccessTokenClaims> claimsCaptor = ArgumentCaptor.forClass(AccessTokenClaims.class);
            verify(jwtTokenIssuer).createAccessToken(claimsCaptor.capture(), eq(ACCESS_EXPIRY));
            AccessTokenClaims claims = claimsCaptor.getValue();
            assertThat(claims.getUserId()).isEqualTo(user.getId());
            assertThat(claims.getEmail()).isEqualTo("user@example.com");
            assertThat(claims.getRole()).isEqualTo("CLIENT");
            assertThat(claims.getTenantId()).isEqualTo(user.getTenant().getId());

            verify(refreshTokenCreator).create(user, REFRESH_EXPIRY);
        }

        @Test
        @DisplayName("throws InvalidRefreshTokenException when redeemer throws")
        void invalidToken() {
            when(refreshTokenRedeemer.redeem(RAW_REFRESH_TOKEN))
                .thenThrow(new InvalidRefreshTokenException("Invalid or expired refresh token."));

            assertThatThrownBy(() -> refreshTokenService.refresh(RAW_REFRESH_TOKEN))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("Invalid or expired refresh token");

            verify(jwtTokenIssuer, never()).createAccessToken(any(), any(Long.class));
            verify(refreshTokenCreator, never()).create(any(User.class), any(Long.class));
        }

        @Test
        @DisplayName("throws InvalidRefreshTokenException when user is inactive")
        void userInactive() {
            user.setActive(false);
            when(refreshTokenRedeemer.redeem(RAW_REFRESH_TOKEN)).thenReturn(user);

            assertThatThrownBy(() -> refreshTokenService.refresh(RAW_REFRESH_TOKEN))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("Invalid or expired refresh token");

            verify(jwtTokenIssuer, never()).createAccessToken(any(), any(Long.class));
            verify(refreshTokenCreator, never()).create(any(User.class), any(Long.class));
        }
    }
}
