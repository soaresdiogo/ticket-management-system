package com.di2it.api_gateway.infrastructure.filter;

import com.di2it.api_gateway.application.domain.PropagatedUserHeaders;
import com.di2it.api_gateway.application.port.JwtUserContextExtractor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.Instant;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("UserContextPropagationGlobalFilter")
class UserContextPropagationGlobalFilterTest {

    private JwtUserContextExtractor extractor;
    private UserContextPropagationGlobalFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        extractor = mock(JwtUserContextExtractor.class);
        filter = new UserContextPropagationGlobalFilter(extractor);
        chain = mock(GatewayFilterChain.class);
    }

    @Test
    @DisplayName("adds X-User-Id, X-User-Role, X-Tenant-Id when principal is JWT and extract returns headers")
    void authenticatedRequest_propagatesHeaders() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "RS256").subject("user-123")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(3600))
                .claim("role", "ACCOUNTANT").claim("tenantId", "tenant-456").build();
        PropagatedUserHeaders headers = new PropagatedUserHeaders("user-123", "ACCOUNTANT", "tenant-456");
        when(extractor.extract(jwt)).thenReturn(Optional.of(headers));

        MockServerHttpRequest baseRequest = MockServerHttpRequest.get("/tickets/1").build();
        ServerWebExchange delegate = MockServerWebExchange.builder(baseRequest).build();
        ServerWebExchange exchange = exchangeWithPrincipal(delegate, jwt);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        var exchangeCaptor = org.mockito.ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(exchangeCaptor.capture());
        ServerWebExchange capturedExchange = exchangeCaptor.getValue();
        HttpHeaders requestHeaders = capturedExchange.getRequest().getHeaders();

        assertThat(requestHeaders.getFirst(PropagatedUserHeaders.HEADER_USER_ID)).isEqualTo("user-123");
        assertThat(requestHeaders.getFirst(PropagatedUserHeaders.HEADER_USER_ROLE)).isEqualTo("ACCOUNTANT");
        assertThat(requestHeaders.getFirst(PropagatedUserHeaders.HEADER_TENANT_ID)).isEqualTo("tenant-456");
    }

    @Test
    @DisplayName("does not add headers when principal is not JWT and continues chain")
    void noJwtPrincipal_continuesWithoutHeaders() {
        MockServerHttpRequest baseRequest = MockServerHttpRequest.get("/auth/login").build();
        ServerWebExchange exchange = MockServerWebExchange.builder(baseRequest).build();
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        var exchangeCaptor = org.mockito.ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(exchangeCaptor.capture());
        ServerWebExchange capturedExchange = exchangeCaptor.getValue();
        assertThat(capturedExchange.getRequest().getHeaders()
                .getFirst(PropagatedUserHeaders.HEADER_USER_ID)).isNull();
    }

    @Test
    @DisplayName("does not add headers when extract returns empty and continues chain")
    void extractReturnsEmpty_continuesWithoutHeaders() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "RS256").subject("user-123")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(3600))
                .claim("role", "CLIENT").claim("tenantId", "tid").build();
        when(extractor.extract(jwt)).thenReturn(Optional.empty());

        MockServerHttpRequest baseRequest = MockServerHttpRequest.get("/tickets/1").build();
        ServerWebExchange delegate = MockServerWebExchange.builder(baseRequest).build();
        ServerWebExchange exchange = exchangeWithPrincipal(delegate, jwt);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result).verifyComplete();

        var exchangeCaptor = org.mockito.ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(exchangeCaptor.capture());
        assertThat(exchangeCaptor.getValue().getRequest().getHeaders()
                .getFirst(PropagatedUserHeaders.HEADER_USER_ID)).isNull();
    }

    /** Exchange decorator that returns an Authentication (Principal) whose getPrincipal() is the Jwt. */
    private static ServerWebExchange exchangeWithPrincipal(ServerWebExchange delegate, Jwt jwt) {
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
        return new ServerWebExchangeDecorator(delegate) {
            @Override
            public Mono<Principal> getPrincipal() {
                return Mono.just(authentication);
            }
        };
    }
}
