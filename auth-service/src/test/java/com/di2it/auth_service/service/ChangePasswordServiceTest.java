package com.di2it.auth_service.service;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceTest {

    private static final String CURRENT_PASSWORD = "OldP@ss1";
    private static final String NEW_PASSWORD = "NewP@ss2";
    private static final String HASHED_CURRENT = "$2a$10$hashed";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ChangePasswordService changePasswordService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        Tenant tenant = Tenant.builder()
            .id(UUID.randomUUID())
            .name("Acme")
            .email("admin@acme.com")
            .active(true)
            .build();
        user = User.builder()
            .id(userId)
            .tenant(tenant)
            .email("user@example.com")
            .passwordHash(HASHED_CURRENT)
            .fullName("John Doe")
            .role("CLIENT")
            .active(true)
            .firstAccess(true)
            .build();
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("updates password and sets firstAccess to false when credentials are valid")
        void success() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(CURRENT_PASSWORD, HASHED_CURRENT)).thenReturn(true);
            when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("$2a$10$newHashed");

            changePasswordService.changePassword(userId, CURRENT_PASSWORD, NEW_PASSWORD);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User saved = userCaptor.getValue();
            assertThat(saved.getPasswordHash()).isEqualTo("$2a$10$newHashed");
            assertThat(saved.isFirstAccess()).isFalse();
        }

        @Test
        @DisplayName("keeps firstAccess false when already false")
        void firstAccessAlreadyFalse() {
            user.setFirstAccess(false);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(CURRENT_PASSWORD, HASHED_CURRENT)).thenReturn(true);
            when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("$2a$10$newHashed");

            changePasswordService.changePassword(userId, CURRENT_PASSWORD, NEW_PASSWORD);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().isFirstAccess()).isFalse();
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when user not found")
        void userNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> changePasswordService.changePassword(userId, CURRENT_PASSWORD, NEW_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");

            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when user is inactive")
        void userInactive() {
            user.setActive(false);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> changePasswordService.changePassword(userId, CURRENT_PASSWORD, NEW_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");

            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when current password does not match")
        void wrongCurrentPassword() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(CURRENT_PASSWORD, HASHED_CURRENT)).thenReturn(false);

            assertThatThrownBy(() -> changePasswordService.changePassword(userId, CURRENT_PASSWORD, NEW_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");

            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when current password is null")
        void nullCurrentPassword() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> changePasswordService.changePassword(userId, null, NEW_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");

            verify(userRepository, never()).save(any(User.class));
        }
    }
}
