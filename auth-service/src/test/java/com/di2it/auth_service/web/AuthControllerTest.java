package com.di2it.auth_service.web;

import com.di2it.auth_service.service.ChangePasswordService;
import com.di2it.auth_service.service.DuplicateEmailException;
import com.di2it.auth_service.service.EmailDeliveryException;
import com.di2it.auth_service.service.InvalidCredentialsException;
import com.di2it.auth_service.service.InvalidMfaCodeException;
import com.di2it.auth_service.service.InvalidRefreshTokenException;
import com.di2it.auth_service.service.LoginService;
import com.di2it.auth_service.service.RefreshResult;
import com.di2it.auth_service.service.RefreshTokenService;
import com.di2it.auth_service.service.TenantNotFoundException;
import com.di2it.auth_service.service.UserRegistrationService;
import com.di2it.auth_service.service.VerifyMfaResult;
import com.di2it.auth_service.service.VerifyMfaService;
import com.di2it.auth_service.web.dto.ChangePasswordRequest;
import com.di2it.auth_service.web.dto.ChangePasswordResponse;
import com.di2it.auth_service.web.dto.LoginRequest;
import com.di2it.auth_service.web.dto.LoginResponse;
import com.di2it.auth_service.web.dto.RefreshRequest;
import com.di2it.auth_service.web.dto.RefreshResponse;
import com.di2it.auth_service.web.dto.RegisterUserRequest;
import com.di2it.auth_service.web.dto.RegisterUserResponse;
import com.di2it.auth_service.web.dto.VerifyMfaRequest;
import com.di2it.auth_service.web.dto.VerifyMfaResponse;
import org.springframework.security.oauth2.jwt.Jwt;
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
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserRegistrationService userRegistrationService;

    @Mock
    private LoginService loginService;

    @Mock
    private VerifyMfaService verifyMfaService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private ChangePasswordService changePasswordService;

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
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("returns 200 and message when login succeeds")
        void success() {
            LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("SecureP@ss1")
                .build();

            ResponseEntity<LoginResponse> result = authController.login(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getMessage()).contains("verification code");
            verify(loginService).login("user@example.com", "SecureP@ss1");
        }
    }

    @Nested
    @DisplayName("verifyMfa")
    class VerifyMfa {

        @Test
        @DisplayName("returns 200 with access token and refresh token when verification succeeds")
        void success() {
            VerifyMfaRequest request = VerifyMfaRequest.builder()
                .email("user@example.com")
                .code("123456")
                .includeRefreshToken(true)
                .build();
            VerifyMfaResult serviceResult = VerifyMfaResult.builder()
                .accessToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
                .expiresInSeconds(900)
                .refreshToken("opaque.refresh.token")
                .build();
            when(verifyMfaService.verify("user@example.com", "123456", true)).thenReturn(serviceResult);

            ResponseEntity<VerifyMfaResponse> result = authController.verifyMfa(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getAccessToken()).isEqualTo("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...");
            assertThat(result.getBody().getTokenType()).isEqualTo("Bearer");
            assertThat(result.getBody().getExpiresIn()).isEqualTo(900);
            assertThat(result.getBody().getRefreshToken()).isEqualTo("opaque.refresh.token");
            verify(verifyMfaService).verify("user@example.com", "123456", true);
        }

        @Test
        @DisplayName("defaults includeRefreshToken to true when null")
        void includeRefreshTokenDefaultsTrue() {
            VerifyMfaRequest request = VerifyMfaRequest.builder()
                .email("user@example.com")
                .code("123456")
                .build();
            VerifyMfaResult serviceResult = VerifyMfaResult.builder()
                .accessToken("jwt")
                .expiresInSeconds(900)
                .refreshToken("refresh")
                .build();
            when(verifyMfaService.verify("user@example.com", "123456", true)).thenReturn(serviceResult);

            authController.verifyMfa(request);

            verify(verifyMfaService).verify("user@example.com", "123456", true);
        }
    }

    @Nested
    @DisplayName("refresh")
    class Refresh {

        @Test
        @DisplayName("returns 200 with access and refresh token when refresh succeeds")
        void success() {
            RefreshRequest request = RefreshRequest.builder()
                .refreshToken("opaque.refresh.token")
                .build();
            RefreshResult serviceResult = RefreshResult.builder()
                .accessToken("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
                .expiresInSeconds(900)
                .refreshToken("new.opaque.refresh.token")
                .build();
            when(refreshTokenService.refresh("opaque.refresh.token")).thenReturn(serviceResult);

            ResponseEntity<RefreshResponse> result = authController.refresh(request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getAccessToken()).isEqualTo("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...");
            assertThat(result.getBody().getTokenType()).isEqualTo("Bearer");
            assertThat(result.getBody().getExpiresIn()).isEqualTo(900);
            assertThat(result.getBody().getRefreshToken()).isEqualTo("new.opaque.refresh.token");
            verify(refreshTokenService).refresh("opaque.refresh.token");
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("returns 200 with message when change password succeeds")
        void success() {
            UUID userId = UUID.randomUUID();
            Jwt jwt = mock(Jwt.class);
            when(jwt.getSubject()).thenReturn(userId.toString());
            ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("OldP@ss1")
                .newPassword("NewP@ss2")
                .build();

            ResponseEntity<ChangePasswordResponse> result =
                authController.changePassword(jwt, request);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()).isNotNull();
            assertThat(result.getBody().getMessage()).isEqualTo("Password changed successfully.");
            verify(changePasswordService).changePassword(
                eq(userId),
                eq("OldP@ss1"),
                eq("NewP@ss2")
            );
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

        @Test
        @DisplayName("handleInvalidCredentials returns 401 with error message")
        void invalidCredentials() {
            InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");

            ResponseEntity<Map<String, String>> result =
                authController.handleInvalidCredentials(ex);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(result.getBody()).containsEntry("error", ex.getMessage());
        }

        @Test
        @DisplayName("handleEmailDelivery returns 503 with safe message")
        void emailDeliveryFailure() {
            EmailDeliveryException ex = new EmailDeliveryException("Resend API error");

            ResponseEntity<Map<String, String>> result =
                authController.handleEmailDelivery(ex);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(result.getBody()).containsKey("error");
            assertThat(result.getBody().get("error")).contains("try again");
        }

        @Test
        @DisplayName("handleInvalidMfaCode returns 401 with error message")
        void invalidMfaCode() {
            InvalidMfaCodeException ex =
                new InvalidMfaCodeException("Invalid or expired verification code.");

            ResponseEntity<Map<String, String>> result =
                authController.handleInvalidMfaCode(ex);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(result.getBody()).containsEntry("error", ex.getMessage());
        }

        @Test
        @DisplayName("handleInvalidRefreshToken returns 401 with error message")
        void invalidRefreshToken() {
            InvalidRefreshTokenException ex =
                new InvalidRefreshTokenException("Invalid or expired refresh token.");

            ResponseEntity<Map<String, String>> result =
                authController.handleInvalidRefreshToken(ex);

            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(result.getBody()).containsEntry("error", ex.getMessage());
        }
    }
}
