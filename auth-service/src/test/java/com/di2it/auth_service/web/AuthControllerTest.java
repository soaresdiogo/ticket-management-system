package com.di2it.auth_service.web;

import com.di2it.auth_service.service.DuplicateEmailException;
import com.di2it.auth_service.service.TenantNotFoundException;
import com.di2it.auth_service.service.UserRegistrationService;
import com.di2it.auth_service.web.dto.RegisterUserRequest;
import com.di2it.auth_service.web.dto.RegisterUserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRegistrationService userRegistrationService;

    @InjectMocks
    private AuthController authController;

    private UUID tenantId;
    private RegisterUserRequest request;
    private RegisterUserResponse response;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        request = RegisterUserRequest.builder()
            .email("user@example.com")
            .password("SecureP@ss1")
            .fullName("John Doe")
            .role("agent")
            .build();
        response = RegisterUserResponse.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .email("user@example.com")
            .fullName("John Doe")
            .role("AGENT")
            .active(true)
            .firstAccess(true)
            .build();
    }

    @Nested
    @DisplayName("registerUser")
    class RegisterUser {

        @Test
        @DisplayName("returns 201 and response body when registration succeeds")
        void success() {
            when(userRegistrationService.register(eq(tenantId), any(RegisterUserRequest.class)))
                .thenReturn(response);

            ResponseEntity<RegisterUserResponse> result =
                authController.registerUser(tenantId, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getEmail()).isEqualTo("user@example.com");
            assertThat(result.getBody().getTenantId()).isEqualTo(tenantId);
            assertThat(result.getBody().getRole()).isEqualTo("AGENT");
            verify(userRegistrationService).register(tenantId, request);
        }
    }

    @Nested
    @DisplayName("exception handlers")
    class ExceptionHandlers {

        @Test
        @DisplayName("handleTenantNotFound returns 404 with error message")
        void tenantNotFound() {
            TenantNotFoundException ex =
                new TenantNotFoundException("Tenant not found: " + tenantId);

            ResponseEntity<Map<String, String>> result =
                authController.handleTenantNotFound(ex);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(result.getBody()).containsEntry("error", ex.getMessage());
        }

        @Test
        @DisplayName("handleDuplicateEmail returns 409 with error message")
        void duplicateEmail() {
            DuplicateEmailException ex =
                new DuplicateEmailException("A user with email user@example.com already exists");

            ResponseEntity<Map<String, String>> result =
                authController.handleDuplicateEmail(ex);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(result.getBody()).containsEntry("error", ex.getMessage());
        }
    }
}
