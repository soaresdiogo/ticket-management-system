package com.di2it.api_gateway.config;

import com.di2it.api_gateway.application.port.AuthPublicKeyProvider;
import com.di2it.api_gateway.infrastructure.jwt.PemKeyParser;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import reactor.core.publisher.Mono;

/**
 * Configures JWT validation for the gateway using the auth-service public key.
 * The decoder lazily fetches and caches the public key from auth-service.
 */
@Configuration
@Profile("!test")
public class JwtDecoderConfig {

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(AuthPublicKeyProvider authPublicKeyProvider) {
        return new CachingReactiveJwtDecoder(authPublicKeyProvider);
    }

    /**
     * ReactiveJwtDecoder that obtains the public key from AuthPublicKeyProvider,
     * builds a NimbusReactiveJwtDecoder, and caches it for subsequent requests.
     */
    private static final class CachingReactiveJwtDecoder implements ReactiveJwtDecoder {

        private final AuthPublicKeyProvider provider;
        private final java.util.concurrent.atomic.AtomicReference<DecoderHolder> cache =
                new java.util.concurrent.atomic.AtomicReference<>();

        CachingReactiveJwtDecoder(AuthPublicKeyProvider provider) {
            this.provider = provider;
        }

        @Override
        public Mono<Jwt> decode(String token) {
            return provider.getPublicKeyPem()
                    .flatMap(this::getOrCreateDecoder)
                    .flatMap(decoder -> decoder.decode(token));
        }

        private Mono<ReactiveJwtDecoder> getOrCreateDecoder(String pem) {
            DecoderHolder holder = cache.get();
            if (holder != null && pem.equals(holder.pem)) {
                return Mono.just(holder.decoder);
            }
            var key = PemKeyParser.parsePublicKey(pem);
            ReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withPublicKey(key).build();
            cache.set(new DecoderHolder(pem, decoder));
            return Mono.just(decoder);
        }

        private static final class DecoderHolder {
            final String pem;
            final ReactiveJwtDecoder decoder;

            DecoderHolder(String pem, ReactiveJwtDecoder decoder) {
                this.pem = pem;
                this.decoder = decoder;
            }
        }
    }
}
