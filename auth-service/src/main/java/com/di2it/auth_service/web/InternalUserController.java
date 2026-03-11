package com.di2it.auth_service.web;

import com.di2it.auth_service.domain.repository.UserRepository;
import com.di2it.auth_service.web.dto.InternalUserEmailResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Internal endpoints for other services (e.g. notification-service) to resolve user data.
 * Not intended for direct client access; protect at gateway or network level.
 */
@Tag(name = "Internal", description = "Internal service-to-service endpoints (e.g. user email lookup)")
@RestController
@RequestMapping("/auth/internal")
public class InternalUserController {

    private final UserRepository userRepository;

    public InternalUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Operation(
        summary = "Get user email by ID",
        description = "Returns user id and email. Used by notification-service for email delivery.")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/users/{id}")
    public ResponseEntity<InternalUserEmailResponse> getUserEmail(
        @Parameter(description = "User ID") @PathVariable UUID id
    ) {
        return userRepository.findById(id)
            .map(user -> new InternalUserEmailResponse(user.getId(), user.getEmail()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
