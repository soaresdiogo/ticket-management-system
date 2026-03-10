package com.di2it.auth_service.service;

import com.di2it.auth_service.application.MfaCodeGenerator;
import com.di2it.auth_service.application.port.MfaCodeStorage;
import com.di2it.auth_service.application.port.MfaEmailSender;
import com.di2it.auth_service.config.MfaProperties;
import com.di2it.auth_service.domain.entity.User;
import com.di2it.auth_service.domain.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.Locale;

/**
 * Use case: validate credentials, generate MFA code, store in Redis with TTL, send via email.
 */
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MfaCodeStorage mfaCodeStorage;
    private final MfaEmailSender mfaEmailSender;
    private final MfaProperties mfaProperties;

    /**
     * Validates email/password, generates a one-time MFA code, stores it in Redis with TTL,
     * and sends it to the user's email. Does not reveal whether the account exists on failure.
     *
     * @param email    user email (will be normalized to lowercase)
     * @param password plain text password
     * @throws InvalidCredentialsException when user not found or password does not match
     * @throws EmailDeliveryException      when sending the MFA email fails
     */
    public void login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        User user = findActiveUser(normalizedEmail);
        validatePassword(user, password);
        String code = MfaCodeGenerator.generate(mfaProperties.getCodeLength());
        mfaCodeStorage.store(normalizedEmail, code, mfaProperties.getTtlSeconds());
        mfaEmailSender.sendMfaCode(normalizedEmail, code);
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private User findActiveUser(String email) {
        return userRepository.findByEmail(email)
            .filter(User::isActive)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
    }

    private void validatePassword(User user, String rawPassword) {
        if (rawPassword == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }
}
