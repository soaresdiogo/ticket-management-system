package com.di2it.auth_service.application;

import java.security.SecureRandom;

/**
 * Generates numeric MFA codes (e.g. 6 digits).
 */
public final class MfaCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private MfaCodeGenerator() {
    }

    /**
     * Generates a numeric code of the given length (e.g. 6 for "123456").
     */
    public static String generate(int length) {
        if (length <= 0 || length > 10) {
            throw new IllegalArgumentException("Length must be between 1 and 10");
        }
        int max = (int) Math.pow(10, length);
        int value = RANDOM.nextInt(max);
        return String.format("%0" + length + "d", value);
    }
}
