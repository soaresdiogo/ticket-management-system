package com.di2it.auth_service.service;

import com.di2it.auth_service.application.port.MfaCodeStorage;
import com.di2it.auth_service.application.port.MfaEmailSender;
import com.di2it.auth_service.config.MfaProperties;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MfaCodeStorage mfaCodeStorage;

    @Mock
    private MfaEmailSender mfaEmailSender;

    @Mock
    private MfaProperties mfaProperties;

    @InjectMocks
    private LoginService loginService;

    private User user;
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "SecureP@ss1";
    private static final String PASSWORD_HASH = "encoded-hash";
    private static final int CODE_LENGTH = 6;
    private static final long TTL_SECONDS = 300L;

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
            .passwordHash(PASSWORD_HASH)
            .fullName("John Doe")
            .role("CLIENT")
            .active(true)
            .firstAccess(true)
            .build();
    }

    private void stubMfaProperties() {
        when(mfaProperties.getCodeLength()).thenReturn(CODE_LENGTH);
        when(mfaProperties.getTtlSeconds()).thenReturn(TTL_SECONDS);
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("stores MFA code and sends email when credentials are valid")
        void success() {
            stubMfaProperties();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);

            loginService.login(EMAIL, PASSWORD);

            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
            verify(mfaCodeStorage).store(emailCaptor.capture(), codeCaptor.capture(), eq(TTL_SECONDS));
            assertThat(emailCaptor.getValue()).isEqualTo(EMAIL);
            assertThat(codeCaptor.getValue()).hasSize(CODE_LENGTH).matches("\\d+");

            verify(mfaEmailSender).sendMfaCode(eq(EMAIL), eq(codeCaptor.getValue()));
        }

        @Test
        @DisplayName("normalizes email to lowercase")
        void normalizesEmail() {
            stubMfaProperties();
            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);

            loginService.login("  User@Example.COM  ", PASSWORD);

            verify(mfaCodeStorage).store(eq("user@example.com"), anyString(), anyLong());
            verify(mfaEmailSender).sendMfaCode(eq("user@example.com"), anyString());
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when user not found")
        void userNotFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loginService.login(EMAIL, PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

            verify(mfaCodeStorage, never()).store(anyString(), anyString(), anyLong());
            verify(mfaEmailSender, never()).sendMfaCode(anyString(), anyString());
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when user is inactive")
        void userInactive() {
            user.setActive(false);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> loginService.login(EMAIL, PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

            verify(mfaCodeStorage, never()).store(anyString(), anyString(), anyLong());
            verify(mfaEmailSender, never()).sendMfaCode(anyString(), anyString());
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when password does not match")
        void wrongPassword() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(false);

            assertThatThrownBy(() -> loginService.login(EMAIL, PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

            verify(mfaCodeStorage, never()).store(anyString(), anyString(), anyLong());
            verify(mfaEmailSender, never()).sendMfaCode(anyString(), anyString());
        }
    }
}
