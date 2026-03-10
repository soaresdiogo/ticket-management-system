package com.di2it.api_gateway.infrastructure.auth;

import com.di2it.api_gateway.application.port.AuthPublicKeyProvider;
import com.di2it.api_gateway.config.AuthServiceProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthServicePublicKeyProvider")
class AuthServicePublicKeyProviderTest {

    private static final String AUTH_BASE_URL = "http://auth:8081";
    private static final String PEM = "-----BEGIN PUBLIC KEY-----\nMIIBIjAN...\n-----END PUBLIC KEY-----";

    private AuthServiceProperties properties;

    @BeforeEach
    void setUp() {
        properties = new AuthServiceProperties();
        properties.setUrl(AUTH_BASE_URL);
    }

    private static String jsonResponse(String publicKey) {
        String escaped = publicKey == null ? "null"
                : "\"" + publicKey.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
        return "{\"publicKey\":" + escaped + ",\"keyId\":\"key-1\",\"algorithm\":\"RS256\"}";
    }

    private static ClientResponse okJson(String json) {
        return ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .build();
    }

    @Nested
    @DisplayName("getPublicKeyPem")
    class GetPublicKeyPem {

        @Test
        @DisplayName("fetches public key from auth-service and caches it")
        void fetchesAndCaches() {
            ExchangeFunction exchange = request -> Mono.just(okJson(jsonResponse(PEM)));
            AuthPublicKeyProvider provider = new AuthServicePublicKeyProvider(properties,
                    WebClient.builder().exchangeFunction(exchange));

            StepVerifier.create(provider.getPublicKeyPem())
                    .expectNext(PEM)
                    .verifyComplete();

            StepVerifier.create(provider.getPublicKeyPem())
                    .expectNext(PEM)
                    .verifyComplete();
        }

        @Test
        @DisplayName("normalizes auth URL without trailing slash")
        void normalizesUrl() {
            properties.setUrl("http://auth:8081/");
            ExchangeFunction exchange = request -> Mono.just(okJson(jsonResponse(PEM)));
            AuthPublicKeyProvider provider = new AuthServicePublicKeyProvider(properties,
                    WebClient.builder()
                            .exchangeFunction(exchange));

            StepVerifier.create(provider.getPublicKeyPem())
                    .expectNext(PEM)
                    .verifyComplete();
        }

        @Test
        @DisplayName("errors when auth-service returns empty publicKey")
        void emptyPublicKey_errors() {
            ExchangeFunction exchange = request -> Mono.just(
                    okJson("{\"publicKey\":\"\",\"keyId\":null,\"algorithm\":null}"));
            AuthPublicKeyProvider provider = new AuthServicePublicKeyProvider(properties,
                    WebClient.builder().exchangeFunction(exchange));

            StepVerifier.create(provider.getPublicKeyPem())
                    .expectError(IllegalStateException.class)
                    .verify();
        }

        @Test
        @DisplayName("errors when auth-service returns null publicKey")
        void nullPublicKey_errors() {
            ExchangeFunction exchange = request -> Mono.just(
                    okJson("{\"publicKey\":null,\"keyId\":null,\"algorithm\":null}"));
            AuthPublicKeyProvider provider = new AuthServicePublicKeyProvider(properties,
                    WebClient.builder().exchangeFunction(exchange));

            StepVerifier.create(provider.getPublicKeyPem())
                    .expectError(IllegalStateException.class)
                    .verify();
        }

        @Test
        @DisplayName("propagates WebClient error with message")
        void webClientError_propagates() {
            ExchangeFunction exchange = request -> Mono.error(
                    WebClientResponseException.create(502, "Bad Gateway", null, null, null));
            AuthPublicKeyProvider provider = new AuthServicePublicKeyProvider(properties,
                    WebClient.builder().exchangeFunction(exchange));

            StepVerifier.create(provider.getPublicKeyPem())
                    .expectErrorSatisfies(e -> {
                        assertThat(e).isInstanceOf(IllegalStateException.class);
                        assertThat(e.getCause()).isInstanceOf(WebClientResponseException.class);
                    })
                    .verify();
        }
    }
}
