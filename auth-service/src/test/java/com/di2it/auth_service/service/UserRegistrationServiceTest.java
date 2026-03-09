package com.di2it.auth_service.service;

import com.di2it.auth_service.domain.entity.Tenant;
import com.di2it.auth_service.domain.entity.User;
import com.di2it.auth_service.domain.repository.TenantRepository;
import com.di2it.auth_service.domain.repository.UserRepository;
import com.di2it.auth_service.web.dto.RegisterUserRequest;
import com.di2it.auth_service.web.dto.RegisterUserResponse;
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
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    private UUID tenantId;
    private Tenant tenant;
    private RegisterUserRequest request;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        tenant = Tenant.builder()
            .id(tenantId)
            .name("Acme Corp")
            .email("admin@acme.com")
            .active(true)
            .build();
        request = RegisterUserRequest.builder()
            .email("user@example.com")
            .password("SecureP@ss1")
            .fullName("John Doe")
            .role("agent")
            .build();
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("creates user and returns response when tenant exists and email is unique")
        void success() {
            when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
            when(passwordEncoder.encode("SecureP@ss1")).thenReturn("encoded-password");

            User savedUser = User.builder()
                .id(UUID.randomUUID())
                .tenant(tenant)
                .email("user@example.com")
                .passwordHash("encoded-password")
                .fullName("John Doe")
                .role("AGENT")
                .active(true)
                .firstAccess(true)
                .build();
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                if (u.getId() == null) {
                    return User.builder()
                        .id(savedUser.getId())
                        .tenant(u.getTenant())
                        .email(u.getEmail())
                        .passwordHash(u.getPasswordHash())
                        .fullName(u.getFullName())
                        .role(u.getRole())
                        .active(u.isActive())
                        .firstAccess(u.isFirstAccess())
                        .build();
                }
                return u;
            });

            RegisterUserResponse response = userRegistrationService.register(tenantId, request);

            assertThat(response).isNotNull();
            assertThat(response.getTenantId()).isEqualTo(tenantId);
            assertThat(response.getEmail()).isEqualTo("user@example.com");
            assertThat(response.getFullName()).isEqualTo("John Doe");
            assertThat(response.getRole()).isEqualTo("AGENT");
            assertThat(response.isActive()).isTrue();
            assertThat(response.isFirstAccess()).isTrue();

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User captured = userCaptor.getValue();
            assertThat(captured.getEmail()).isEqualTo("user@example.com");
            assertThat(captured.getPasswordHash()).isEqualTo("encoded-password");
            assertThat(captured.getFullName()).isEqualTo("John Doe");
            assertThat(captured.getRole()).isEqualTo("AGENT");
            assertThat(captured.getTenant()).isEqualTo(tenant);
        }

        @Test
        @DisplayName("normalizes email to lowercase and role to uppercase")
        void normalizesEmailAndRole() {
            request.setEmail("  User@Example.COM  ");
            request.setRole("  admin  ");
            when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(userRepository.existsByEmail("  User@Example.COM  ")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");

            User saved = User.builder()
                .id(UUID.randomUUID())
                .tenant(tenant)
                .email("user@example.com")
                .passwordHash("encoded")
                .fullName("John Doe")
                .role("ADMIN")
                .active(true)
                .firstAccess(true)
                .build();
            when(userRepository.save(any(User.class))).thenReturn(saved);

            RegisterUserResponse response = userRegistrationService.register(tenantId, request);

            assertThat(response.getEmail()).isEqualTo("user@example.com");
            assertThat(response.getRole()).isEqualTo("ADMIN");
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getEmail()).isEqualTo("user@example.com");
            assertThat(userCaptor.getValue().getRole()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("throws TenantNotFoundException when tenant does not exist")
        void tenantNotFound() {
            when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userRegistrationService.register(tenantId, request))
                .isInstanceOf(TenantNotFoundException.class)
                .hasMessageContaining("Tenant not found")
                .hasMessageContaining(tenantId.toString());

            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("throws DuplicateEmailException when email already exists")
        void duplicateEmail() {
            when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
            when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userRegistrationService.register(tenantId, request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("user@example.com")
                .hasMessageContaining("already exists");

            verify(userRepository, never()).save(any(User.class));
        }
    }
}
