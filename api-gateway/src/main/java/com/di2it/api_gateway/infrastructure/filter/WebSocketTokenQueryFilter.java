package com.di2it.api_gateway.infrastructure.filter;

import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Runs before the security filter chain. For WebSocket handshake requests to /ws/**,
 * copies the access_token query parameter into the Authorization header so the security
 * chain can validate the JWT. SockJS/browser cannot set custom headers on the initial
 * GET /ws/info, so the client sends the token in the URL.
 */
@Component
public class WebSocketTokenQueryFilter implements WebFilter, Ordered {

    private static final String WS_PATH_PREFIX = "/ws";
    private static final String ACCESS_TOKEN_PARAM = "access_token";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /** Run before Spring Security (default order -100). */
    private static final int ORDER_BEFORE_SECURITY = -150;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (!request.getPath().value().startsWith(WS_PATH_PREFIX)) {
            return chain.filter(exchange);
        }
        List<String> tokens = request.getQueryParams().get(ACCESS_TOKEN_PARAM);
        if (tokens == null || tokens.isEmpty() || tokens.get(0).isBlank()) {
            return chain.filter(exchange);
        }
        String token = tokens.get(0).trim();
        ServerHttpRequest mutated = request.mutate()
            .header(AUTHORIZATION_HEADER, BEARER_PREFIX + token)
            .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() {
        return ORDER_BEFORE_SECURITY;
    }
}
