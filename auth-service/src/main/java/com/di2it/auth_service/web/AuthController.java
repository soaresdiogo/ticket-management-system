package com.di2it.auth_service.web;

import com.di2it.auth_service.service.DuplicateEmailException;
import com.di2it.auth_service.service.EmailDeliveryException;
import com.di2it.auth_service.service.InvalidCredentialsException;
import com.di2it.auth_service.service.LoginService;
import com.di2it.auth_service.service.TenantNotFoundException;
import com.di2it.auth_service.service.UserRegistrationService;
import com.di2it.auth_service.web.dto.LoginRequest;
import com.di2it.auth_service.web.dto.LoginResponse;
import com.di2it.auth_service.web.dto.RegisterUserRequest;
import com.di2it.auth_service.web.dto.RegisterUserResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private final UserRegistrationService userRegistrationService;
    private final LoginService loginService;

    public AuthController(UserRegistrationService userRegistrationService, LoginService loginService) {
        this.userRegistrationService = userRegistrationService;
        this.loginService = loginService;
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
