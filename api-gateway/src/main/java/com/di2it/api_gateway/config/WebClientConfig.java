package com.di2it.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Provides WebClient.Builder for outbound HTTP calls (e.g. auth-service public key).
 * Required because Spring Cloud Gateway does not auto-configure a default WebClient builder.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
