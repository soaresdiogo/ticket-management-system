package com.di2it.auth_service.application.port;

import java.util.Optional;

/**
 * Port for storing and retrieving MFA codes with TTL (e.g. Redis).
 */
public interface MfaCodeStorage {

    /**
     * Stores the MFA code for the given email with the specified TTL in seconds.
     */
    void store(String email, String code, long ttlSeconds);

    /**
     * Returns the stored code for the email, or empty if not found or expired.
     */
    Optional<String> get(String email);

    /**
     * Removes the stored code for the email (e.g. after successful verification).
     */
    void remove(String email);
}
