package com.di2it.auth_service.service;

import com.di2it.auth_service.domain.entity.User;
import com.di2it.auth_service.domain.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Use case: change authenticated user's password and set first_access = false.
 */
@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Validates current password, updates to new password, and sets first_access to false.
     *
     * @param userId          authenticated user id (e.g. from JWT subject)
     * @param currentPassword plain text current password
     * @param newPassword     plain text new password
     * @throws InvalidCredentialsException when user not found, inactive, or current password does not match
     */
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = findActiveUser(userId);
        validateCurrentPassword(user, currentPassword);
        updatePasswordAndFirstAccess(user, newPassword);
    }

    private User findActiveUser(UUID userId) {
        return userRepository.findById(userId)
            .filter(User::isActive)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
    }

    private void validateCurrentPassword(User user, String currentPassword) {
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }

    private void updatePasswordAndFirstAccess(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFirstAccess(false);
        userRepository.save(user);
    }
}
