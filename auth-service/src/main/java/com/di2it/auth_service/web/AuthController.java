package com.di2it.auth_service.web;

import com.di2it.auth_service.service.ChangePasswordService;
import com.di2it.auth_service.service.DuplicateEmailException;
import com.di2it.auth_service.service.EmailDeliveryException;
import com.di2it.auth_service.service.InvalidCredentialsException;
import com.di2it.auth_service.service.InvalidMfaCodeException;
import com.di2it.auth_service.service.InvalidRefreshTokenException;
import com.di2it.auth_service.service.LoginService;
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
import com.di2it.auth_service.web.mapper.RefreshResponseMapper;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Auth endpoints: registration, login, MFA, refresh, etc.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String MFA_SENT_MESSAGE = "If an account exists, a verification code has been sent to your email.";

    private static final String PASSWORD_CHANGED_MESSAGE = "Password changed successfully.";

    private final UserRegistrationService userRegistrationService;
    private final LoginService loginService;
    private final VerifyMfaService verifyMfaService;
    private final RefreshTokenService refreshTokenService;
    private final ChangePasswordService changePasswordService;

    public AuthController(
        UserRegistrationService userRegistrationService,
        LoginService loginService,
        VerifyMfaService verifyMfaService,
        RefreshTokenService refreshTokenService,
        ChangePasswordService changePasswordService
    ) {
        this.userRegistrationService = userRegistrationService;
        this.loginService = loginService;
        this.verifyMfaService = verifyMfaService;
        this.refreshTokenService = refreshTokenService;
        this.changePasswordService = changePasswordService;
    }

    /**
     * Register a new user for a tenant (e.g. created by office/tenant admin).
     * POST /auth/tenants/{tenantId}/users
     */
    @PostMapping("/tenants/{tenantId}/users")
    public ResponseEntity<RegisterUserResponse> registerUser(
        @PathVariable UUID tenantId,
        @Valid @RequestBody RegisterUserRequest request
    ) {
        RegisterUserResponse response = userRegistrationService.register(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login: validate credentials, generate MFA code, store in Redis with TTL, send via Resend.
     * POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        loginService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(LoginResponse.builder().message(MFA_SENT_MESSAGE).build());
    }

    /**
     * Verify MFA code from Redis, issue RS256 JWT and optional refresh token.
     * POST /auth/verify-mfa
     */
    @PostMapping("/verify-mfa")
    public ResponseEntity<VerifyMfaResponse> verifyMfa(@Valid @RequestBody VerifyMfaRequest request) {
        boolean includeRefresh = request.getIncludeRefreshToken() == null || request.getIncludeRefreshToken();
        VerifyMfaResult result = verifyMfaService.verify(
            request.getEmail(),
            request.getCode(),
            includeRefresh
        );
        VerifyMfaResponse response = VerifyMfaResponse.builder()
            .accessToken(result.getAccessToken())
            .tokenType(VerifyMfaResponse.TOKEN_TYPE_BEARER)
            .expiresIn(result.getExpiresInSeconds())
            .refreshToken(result.getRefreshToken())
            .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh: validate refresh token, issue new access token and new refresh token (rotation).
     * POST /auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = RefreshResponseMapper.toResponse(
            refreshTokenService.refresh(request.getRefreshToken())
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Change password for the authenticated user (JWT required). Sets first_access = false.
     * POST /auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody ChangePasswordRequest request
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        changePasswordService.changePassword(
            userId,
            request.getCurrentPassword(),
            request.getNewPassword()
        );
        return ResponseEntity.ok(
            ChangePasswordResponse.builder().message(PASSWORD_CHANGED_MESSAGE).build()
        );
    }

    @ExceptionHandler(InvalidMfaCodeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidMfaCode(InvalidMfaCodeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTenantNotFound(TenantNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<Map<String, String>> handleEmailDelivery(EmailDeliveryException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of("error", "Unable to send verification email. Please try again later."));
    }
}
