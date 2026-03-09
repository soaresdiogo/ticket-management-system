package com.di2it.auth_service.infrastructure.redis;

import com.di2it.auth_service.application.port.MfaCodeStorage;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis implementation of MFA code storage with TTL.
 */
@Component
public class RedisMfaCodeStorage implements MfaCodeStorage {

    private static final String KEY_PREFIX = "mfa:login:";

    private final ValueOperations<String, String> valueOps;

    public RedisMfaCodeStorage(StringRedisTemplate stringRedisTemplate) {
        this.valueOps = stringRedisTemplate.opsForValue();
    }

    @Override
    public void store(String email, String code, long ttlSeconds) {
        String key = keyFor(email);
        valueOps.set(key, code, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public Optional<String> get(String email) {
        String key = keyFor(email);
        String value = valueOps.get(key);
        return Optional.ofNullable(value);
    }

    @Override
    public void remove(String email) {
        String key = keyFor(email);
        valueOps.getOperations().delete(key);
    }

    private static String keyFor(String email) {
        return KEY_PREFIX + email;
    }
}
