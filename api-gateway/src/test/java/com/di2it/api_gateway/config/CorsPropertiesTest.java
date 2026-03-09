package com.di2it.api_gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CorsProperties")
class CorsPropertiesTest {

    @Nested
    @DisplayName("defaults")
    class Defaults {

        @Test
        @DisplayName("has default allowed origin for Angular dev server")
        void defaultAllowedOrigins() {
            CorsProperties props = new CorsProperties();
            assertThat(props.getAllowedOrigins())
                    .containsExactly("http://localhost:4200");
        }

        @Test
        @DisplayName("has default allowed methods including OPTIONS for preflight")
        void defaultAllowedMethods() {
            CorsProperties props = new CorsProperties();
            assertThat(props.getAllowedMethods())
                    .containsExactlyInAnyOrder("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        }

        @Test
        @DisplayName("allows all headers by default")
        void defaultAllowedHeaders() {
            CorsProperties props = new CorsProperties();
            assertThat(props.getAllowedHeaders()).containsExactly("*");
        }

        @Test
        @DisplayName("allowCredentials is true by default for JWT/cookies")
        void defaultAllowCredentials() {
            CorsProperties props = new CorsProperties();
            assertThat(props.isAllowCredentials()).isTrue();
        }

        @Test
        @DisplayName("maxAgeSeconds is 3600 by default")
        void defaultMaxAge() {
            CorsProperties props = new CorsProperties();
            assertThat(props.getMaxAgeSeconds()).isEqualTo(3600L);
        }
    }

    @Nested
    @DisplayName("binding from properties")
    class Binding {

        @SpringBootTest(classes = CorsPropertiesBindingTestConfig.class)
        @ActiveProfiles("test")
        @TestPropertySource(properties = {
                "gateway.cors.allowed-origins[0]=https://app.example.com",
                "gateway.cors.allowed-origins[1]=https://admin.example.com",
                "gateway.cors.allowed-methods[0]=GET",
                "gateway.cors.allowed-methods[1]=POST",
                "gateway.cors.allow-credentials=false",
                "gateway.cors.max-age-seconds=7200"
        })
        @DisplayName("binds list and scalar values from environment")
        static class BindFromProperties {

            @Autowired
            private CorsProperties corsProperties;

            @org.junit.jupiter.api.Test
            @DisplayName("allowed origins are bound")
            void allowedOriginsBound() {
                assertThat(corsProperties.getAllowedOrigins())
                        .containsExactly("https://app.example.com", "https://admin.example.com");
            }

            @org.junit.jupiter.api.Test
            @DisplayName("allowed methods are bound")
            void allowedMethodsBound() {
                assertThat(corsProperties.getAllowedMethods())
                        .containsExactly("GET", "POST");
            }

            @org.junit.jupiter.api.Test
            @DisplayName("allowCredentials is bound")
            void allowCredentialsBound() {
                assertThat(corsProperties.isAllowCredentials()).isFalse();
            }

            @org.junit.jupiter.api.Test
            @DisplayName("maxAgeSeconds is bound")
            void maxAgeBound() {
                assertThat(corsProperties.getMaxAgeSeconds()).isEqualTo(7200L);
            }
        }
    }

    @EnableConfigurationProperties(CorsProperties.class)
    static class CorsPropertiesBindingTestConfig {
    }
}
