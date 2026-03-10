package com.di2it.auth_service.web;

import com.di2it.auth_service.service.ChangePasswordService;
import com.di2it.auth_service.service.DuplicateEmailException;
import com.di2it.auth_service.service.GetPublicKeyService;
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
import com.di2it.auth_service.web.dto.PublicKeyResponse;
import com.di2it.auth_service.web.dto.RefreshRequest;
import com.di2it.auth_service.web.dto.RefreshResponse;
import com.di2it.auth_service.web.dto.RegisterUserRequest;
import com.di2it.auth_service.web.dto.RegisterUserResponse;
import com.di2it.auth_service.web.dto.VerifyMfaRequest;
import com.di2it.auth_service.web.dto.VerifyMfaResponse;
import com.di2it.auth_service.web.mapper.PublicKeyResponseMapper;
import com.di2it.auth_service.web.mapper.RefreshResponseMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
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
@Tag(name = "Auth", description = "Registration, login, MFA verification, token refresh, and password change")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String ERROR_KEY = "error";
    private static final String RESPONSE_CODE_OK = "200";
    private static final String RESPONSE_CODE_UNAUTHORIZED = "401";
    private static final String MFA_SENT_MESSAGE =
        "If an account exists, a verification code has been sent to your email.";

    private static final String PASSWORD_CHANGED_MESSAGE = "Password changed successfully.";

    private final UserRegistrationService userRegistrationService;
    private final LoginService loginService;
    private final VerifyMfaService verifyMfaService;
    private final RefreshTokenService refreshTokenService;
    private final ChangePasswordService changePasswordService;
    private final GetPublicKeyService getPublicKeyService;

    public AuthController(
        UserRegistrationService userRegistrationService,
        LoginService loginService,
        VerifyMfaService verifyMfaService,
        RefreshTokenService refreshTokenService,
        ChangePasswordService changePasswordService,
        GetPublicKeyService getPublicKeyService
    ) {
        this.userRegistrationService = userRegistrationService;
        this.loginService = loginService;
        this.verifyMfaService = verifyMfaService;
        this.refreshTokenService = refreshTokenService;
        this.changePasswordService = changePasswordService;
        this.getPublicKeyService = getPublicKeyService;
    }

    /**
     * Expose public key for gateway and other services to validate JWT (RS256).
     * GET /auth/public-key
     */
    @Operation(
        summary = "Get JWT public key",
        description = "Returns the public key (PEM) for validating RS256 JWTs. Used by gateway and other services.")
    @ApiResponse(responseCode = RESPONSE_CODE_OK, description = "Public key and metadata")
    @GetMapping("/public-key")
    public ResponseEntity<PublicKeyResponse> getPublicKey() {
        PublicKeyResponse response =
            PublicKeyResponseMapper.toResponse(getPublicKeyService.getPublicKey());
        return ResponseEntity.ok(response);
    }

    /**
     * Register a new user for a tenant (e.g. created by office/tenant admin).
     * POST /auth/tenants/{tenantId}/users
     */
    @Operation(
        summary = "Register user",
        description = "Register a new user for the given tenant. No authentication required.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created"),
        @ApiResponse(responseCode = "404", description = "Tenant not found"),
        @ApiResponse(responseCode = "409", description = "Email already in use")
    })
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
    @Operation(
        summary = "Login",
        description = "Validates credentials and sends an MFA code to the user's email. Use verify-mfa for tokens.")
    @ApiResponses({
        @ApiResponse(responseCode = RESPONSE_CODE_OK, description = "MFA code sent (or generic message for security)"),
        @ApiResponse(responseCode = RESPONSE_CODE_UNAUTHORIZED, description = "Invalid credentials"),
        @ApiResponse(responseCode = "503", description = "Email delivery failed")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        loginService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(LoginResponse.builder().message(MFA_SENT_MESSAGE).build());
    }

    /**
     * Verify MFA code from Redis, issue RS256 JWT and optional refresh token.
     * POST /auth/verify-mfa
     */
    @Operation(
        summary = "Verify MFA",
        description = "Verifies the MFA code sent by email and returns an access token (and optional refresh token).")
    @ApiResponses({
        @ApiResponse(responseCode = RESPONSE_CODE_OK, description = "Tokens issued"),
        @ApiResponse(responseCode = RESPONSE_CODE_UNAUTHORIZED, description = "Invalid or expired MFA code")
    })
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
    @Operation(
        summary = "Refresh tokens",
        description = "Exchanges a valid refresh token for a new access token and a new refresh token (rotation).")
    @ApiResponses({
        @ApiResponse(responseCode = RESPONSE_CODE_OK, description = "New tokens issued"),
        @ApiResponse(responseCode = RESPONSE_CODE_UNAUTHORIZED, description = "Invalid or expired refresh token")
    })
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
    @Operation(
        summary = "Change password",
        description = "Changes the authenticated user's password. Requires JWT in Authorization header.",
        security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = RESPONSE_CODE_OK, description = "Password changed"),
        @ApiResponse(
            responseCode = RESPONSE_CODE_UNAUTHORIZED,
            description = "Invalid current password or not authenticated")
    })
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

    private static Map<String, String> errorBody(String message) {
        return Map.of(ERROR_KEY, message);
    }

    @ExceptionHandler(InvalidMfaCodeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidMfaCode(InvalidMfaCodeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody(ex.getMessage()));
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody(ex.getMessage()));
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTenantNotFound(TenantNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody(ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody(ex.getMessage()));
    }

    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<Map<String, String>> handleEmailDelivery(EmailDeliveryException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(errorBody("Unable to send verification email. Please try again later."));
    }
}
