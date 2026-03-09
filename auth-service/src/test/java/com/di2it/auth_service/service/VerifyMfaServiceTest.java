package com.di2it.auth_service.service;

import com.di2it.auth_service.application.AccessTokenClaims;
import com.di2it.auth_service.application.port.JwtTokenIssuer;
import com.di2it.auth_service.application.port.MfaCodeStorage;
import com.di2it.auth_service.application.port.RefreshTokenCreator;
import com.di2it.auth_service.config.JwtKeyProperties;
import com.di2it.auth_service.domain.entity.Tenant;
import com.di2it.auth_service.domain.entity.User;
import com.di2it.auth_service.domain.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyMfaServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String CODE = "123456";
    private static final long ACCESS_EXPIRY = 900L;
    private static final long REFRESH_EXPIRY = 604800L;

    @Mock
    private MfaCodeStorage mfaCodeStorage;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenIssuer jwtTokenIssuer;

    @Mock
    private RefreshTokenCreator refreshTokenCreator;

    @Mock
    private JwtKeyProperties jwtKeyProperties;

    @InjectMocks
    private VerifyMfaService verifyMfaService;

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
            .email(EMAIL)
            .passwordHash("hash")
            .fullName("John Doe")
            .role("CLIENT")
            .active(true)
            .build();

        lenient().when(jwtKeyProperties.getAccessTokenExpirySeconds()).thenReturn(ACCESS_EXPIRY);
        lenient().when(jwtKeyProperties.getRefreshTokenExpirySeconds()).thenReturn(REFRESH_EXPIRY);
    }

    @Nested
    @DisplayName("verify")
    class Verify {

        @Test
        @DisplayName("returns access and refresh token when code is valid and includeRefreshToken is true")
        void successWithRefreshToken() {
            when(mfaCodeStorage.get(EMAIL)).thenReturn(Optional.of(CODE));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(jwtTokenIssuer.createAccessToken(any(AccessTokenClaims.class), eq(ACCESS_EXPIRY)))
                .thenReturn("jwt.access.token");
            when(refreshTokenCreator.create(eq(user), eq(REFRESH_EXPIRY))).thenReturn("opaque.refresh.token");

            VerifyMfaResult result = verifyMfaService.verify(EMAIL, CODE, true);

            assertThat(result.getAccessToken()).isEqualTo("jwt.access.token");
            assertThat(result.getExpiresInSeconds()).isEqualTo(ACCESS_EXPIRY);
            assertThat(result.getRefreshToken()).isEqualTo("opaque.refresh.token");
            assertThat(result.hasRefreshToken()).isTrue();

            verify(mfaCodeStorage).remove(EMAIL);
            ArgumentCaptor<AccessTokenClaims> claimsCaptor = ArgumentCaptor.forClass(AccessTokenClaims.class);
            verify(jwtTokenIssuer).createAccessToken(claimsCaptor.capture(), eq(ACCESS_EXPIRY));
            AccessTokenClaims claims = claimsCaptor.getValue();
            assertThat(claims.getUserId()).isEqualTo(user.getId());
            assertThat(claims.getEmail()).isEqualTo(EMAIL);
            assertThat(claims.getRole()).isEqualTo("CLIENT");
            assertThat(claims.getTenantId()).isEqualTo(user.getTenant().getId());
        }

        @Test
        @DisplayName("returns access token only when includeRefreshToken is false")
        void successWithoutRefreshToken() {
            when(mfaCodeStorage.get(EMAIL)).thenReturn(Optional.of(CODE));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(jwtTokenIssuer.createAccessToken(any(AccessTokenClaims.class), eq(ACCESS_EXPIRY)))
                .thenReturn("jwt.access.token");

            VerifyMfaResult result = verifyMfaService.verify(EMAIL, CODE, false);

            assertThat(result.getAccessToken()).isEqualTo("jwt.access.token");
            assertThat(result.getRefreshToken()).isNull();
            assertThat(result.hasRefreshToken()).isFalse();

            verify(refreshTokenCreator, never()).create(any(User.class), anyLong());
        }

        @Test
        @DisplayName("normalizes email to lowercase")
        void normalizesEmail() {
            when(mfaCodeStorage.get("user@example.com")).thenReturn(Optional.of(CODE));
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(jwtTokenIssuer.createAccessToken(any(AccessTokenClaims.class), eq(ACCESS_EXPIRY)))
                .thenReturn("jwt");

            verifyMfaService.verify("  User@Example.COM  ", CODE, false);

            verify(mfaCodeStorage).get("user@example.com");
            verify(mfaCodeStorage).remove("user@example.com");
        }

        @Test
        @DisplayName("normalizes code by trimming")
        void normalizesCode() {
            when(mfaCodeStorage.get(EMAIL)).thenReturn(Optional.of("123456"));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(jwtTokenIssuer.createAccessToken(any(AccessTokenClaims.class), eq(ACCESS_EXPIRY)))
                .thenReturn("jwt");

            VerifyMfaResult result = verifyMfaService.verify(EMAIL, "  123456  ", false);

            assertThat(result.getAccessToken()).isEqualTo("jwt");
            verify(mfaCodeStorage).remove(EMAIL);
        }

        @Test
        @DisplayName("throws InvalidMfaCodeException when stored code is missing")
        void codeMissing() {
            when(mfaCodeStorage.get(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> verifyMfaService.verify(EMAIL, CODE, false))
                .isInstanceOf(InvalidMfaCodeException.class)
                .hasMessageContaining("Invalid or expired");

            verify(mfaCodeStorage, never()).remove(anyString());
            verify(userRepository, never()).findByEmail(anyString());
            verify(jwtTokenIssuer, never()).createAccessToken(any(AccessTokenClaims.class), anyLong());
        }

        @Test
        @DisplayName("throws InvalidMfaCodeException when code does not match")
        void codeMismatch() {
            when(mfaCodeStorage.get(EMAIL)).thenReturn(Optional.of("999999"));

            assertThatThrownBy(() -> verifyMfaService.verify(EMAIL, CODE, false))
                .isInstanceOf(InvalidMfaCodeException.class)
                .hasMessageContaining("Invalid or expired");

            verify(mfaCodeStorage, never()).remove(anyString());
            verify(jwtTokenIssuer, never()).createAccessToken(any(AccessTokenClaims.class), anyLong());
        }

        @Test
        @DisplayName("throws InvalidMfaCodeException when user not found after consuming code")
        void userNotFound() {
            when(mfaCodeStorage.get(EMAIL)).thenReturn(Optional.of(CODE));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> verifyMfaService.verify(EMAIL, CODE, false))
                .isInstanceOf(InvalidMfaCodeException.class)
                .hasMessageContaining("Invalid or expired");

            verify(mfaCodeStorage).remove(EMAIL);
            verify(jwtTokenIssuer, never()).createAccessToken(any(AccessTokenClaims.class), anyLong());
        }

        @Test
        @DisplayName("throws InvalidMfaCodeException when user is inactive")
        void userInactive() {
            user.setActive(false);
            when(mfaCodeStorage.get(EMAIL)).thenReturn(Optional.of(CODE));
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> verifyMfaService.verify(EMAIL, CODE, false))
                .isInstanceOf(InvalidMfaCodeException.class)
                .hasMessageContaining("Invalid or expired");

            verify(mfaCodeStorage).remove(EMAIL);
            verify(jwtTokenIssuer, never()).createAccessToken(any(AccessTokenClaims.class), anyLong());
        }
    }
}
