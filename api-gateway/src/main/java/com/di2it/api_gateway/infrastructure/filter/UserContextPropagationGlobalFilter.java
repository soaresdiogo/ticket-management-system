package com.di2it.api_gateway.infrastructure.filter;

import com.di2it.api_gateway.application.domain.PropagatedUserHeaders;
import com.di2it.api_gateway.application.port.JwtUserContextExtractor;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Global filter that propagates user context from the validated JWT to downstream services
 * by adding X-User-Id, X-User-Role, and X-Tenant-Id headers to the request.
 * Only runs when the principal is a JWT (authenticated routes); public routes are unchanged.
 */
@Component
public class UserContextPropagationGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUserContextExtractor jwtUserContextExtractor;

    public UserContextPropagationGlobalFilter(JwtUserContextExtractor jwtUserContextExtractor) {
        this.jwtUserContextExtractor = jwtUserContextExtractor;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
            .filter(Authentication.class::isInstance)
            .map(Authentication.class::cast)
            .map(Authentication::getPrincipal)
            .filter(Jwt.class::isInstance)
            .map(Jwt.class::cast)
            .map(jwtUserContextExtractor::extract)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .flatMap(headers -> chain.filter(mutateRequest(exchange, headers)).thenReturn(true))
            .switchIfEmpty(Mono.defer(() -> chain.filter(exchange)).thenReturn(false))
            .then();
    }

    private static ServerWebExchange mutateRequest(ServerWebExchange exchange, PropagatedUserHeaders headers) {
        ServerHttpRequest mutated = exchange.getRequest().mutate()
            .header(PropagatedUserHeaders.HEADER_USER_ID, headers.userId())
            .header(PropagatedUserHeaders.HEADER_USER_ROLE, headers.role())
            .header(PropagatedUserHeaders.HEADER_TENANT_ID, headers.tenantId())
            .build();
        return exchange.mutate().request(mutated).build();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
