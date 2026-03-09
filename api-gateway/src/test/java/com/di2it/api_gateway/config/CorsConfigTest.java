package com.di2it.api_gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CorsConfig")
class CorsConfigTest {

    @Nested
    @DisplayName("toCorsConfiguration")
    class ToCorsConfiguration {

        private CorsConfig corsConfig;
        private CorsProperties corsProperties;

        @BeforeEach
        void setUp() {
            corsConfig = new CorsConfig();
            corsProperties = new CorsProperties();
            corsProperties.setAllowedOrigins(List.of("https://app.example.com"));
            corsProperties.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
            corsProperties.setAllowedHeaders(List.of("Authorization", "Content-Type"));
            corsProperties.setAllowCredentials(true);
            corsProperties.setMaxAgeSeconds(1800L);
        }

        @Test
        @DisplayName("maps allowed origins")
        void mapsAllowedOrigins() {
            CorsConfiguration config = corsConfig.toCorsConfiguration(corsProperties);
            assertThat(config.getAllowedOrigins()).containsExactly("https://app.example.com");
        }

        @Test
        @DisplayName("maps allowed methods")
        void mapsAllowedMethods() {
            CorsConfiguration config = corsConfig.toCorsConfiguration(corsProperties);
            assertThat(config.getAllowedMethods()).containsExactly("GET", "POST", "OPTIONS");
        }

        @Test
        @DisplayName("maps allowed headers")
        void mapsAllowedHeaders() {
            CorsConfiguration config = corsConfig.toCorsConfiguration(corsProperties);
            assertThat(config.getAllowedHeaders()).containsExactly("Authorization", "Content-Type");
        }

        @Test
        @DisplayName("maps allowCredentials")
        void mapsAllowCredentials() {
            CorsConfiguration config = corsConfig.toCorsConfiguration(corsProperties);
            assertThat(config.getAllowCredentials()).isTrue();
        }

        @Test
        @DisplayName("maps maxAge")
        void mapsMaxAge() {
            CorsConfiguration config = corsConfig.toCorsConfiguration(corsProperties);
            assertThat(config.getMaxAge()).isEqualTo(1800L);
        }

        @Test
        @DisplayName("returns new instance per call")
        void returnsNewInstance() {
            CorsConfiguration a = corsConfig.toCorsConfiguration(corsProperties);
            CorsConfiguration b = corsConfig.toCorsConfiguration(corsProperties);
            assertThat(a).isNotSameAs(b);
        }
    }

    @Nested
    @DisplayName("CorsWebFilter bean")
    class CorsWebFilterBean {

        @SpringBootTest
        @ActiveProfiles("test")
        static class Integration {

            @Autowired(required = false)
            private CorsWebFilter corsWebFilter;

            @Test
            @DisplayName("CorsWebFilter bean is present in context")
            void corsWebFilterBeanExists() {
                assertThat(corsWebFilter).isNotNull();
            }
        }
    }
}
