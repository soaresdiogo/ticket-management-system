package com.di2it.api_gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private SecurityWebFilterChain securityWebFilterChain;

    @Test
    @DisplayName("security filter chain bean is present")
    void securityFilterChainBeanExists() {
        assertThat(securityWebFilterChain).isNotNull();
    }
}
