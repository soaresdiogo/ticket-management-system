package com.di2it.auth_service;

import com.di2it.auth_service.application.port.MfaCodeStorage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

@SpringBootTest(properties = {
	"auth.jwt.key-dir=target/test-keys"
})
@ContextConfiguration(classes = AuthServiceApplicationTests.TestConfig.class)
class AuthServiceApplicationTests {

	@Configuration
	static class TestConfig {
		@Bean
		@Primary
		MfaCodeStorage mfaCodeStorage() {
			return new MfaCodeStorage() {
				@Override
				public void store(String email, String code, long ttlSeconds) { }

				@Override
				public Optional<String> get(String email) {
					return Optional.empty();
				}

				@Override
				public void remove(String email) { }
			};
		}
	}

	@Test
	void contextLoads() {
	}
}
