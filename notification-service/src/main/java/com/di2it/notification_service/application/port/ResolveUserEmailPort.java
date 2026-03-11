package com.di2it.notification_service.application.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Port to resolve user email by user id (e.g. via auth-service internal API).
 */
@FunctionalInterface
public interface ResolveUserEmailPort {

    /**
     * Resolves the email address for the given user id.
     *
     * @param userId user id
     * @return email if user exists, empty otherwise
     */
    Optional<String> resolveEmail(UUID userId);
}
