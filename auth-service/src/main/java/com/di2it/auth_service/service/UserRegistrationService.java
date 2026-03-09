package com.di2it.auth_service.service;

import com.di2it.auth_service.domain.entity.Tenant;
import com.di2it.auth_service.domain.entity.User;
import com.di2it.auth_service.domain.repository.TenantRepository;
import com.di2it.auth_service.domain.repository.UserRepository;
import com.di2it.auth_service.web.dto.RegisterUserRequest;
import com.di2it.auth_service.web.dto.RegisterUserResponse;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Registers new users within a tenant (e.g. created by office/tenant admin).
 */
@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register a new user for the given tenant.
     *
     * @param tenantId the tenant the user belongs to
     * @param request  registration data
     * @return the created user (without password)
     * @throws com.di2it.auth_service.service.TenantNotFoundException if tenant does not exist
     * @throws com.di2it.auth_service.service.DuplicateEmailException  if email is already used
     */
    @Transactional
    public RegisterUserResponse register(UUID tenantId, RegisterUserRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + tenantId));

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("A user with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
            .tenant(tenant)
            .email(request.getEmail().trim().toLowerCase())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fullName(request.getFullName().trim())
            .role(request.getRole().trim().toUpperCase())
            .active(true)
            .firstAccess(true)
            .build();

        user = userRepository.save(user);

        return RegisterUserResponse.builder()
            .id(user.getId())
            .tenantId(tenant.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole())
            .active(user.isActive())
            .firstAccess(user.isFirstAccess())
            .build();
    }
}
