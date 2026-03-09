package com.di2it.api_gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GatewayRoutesConfigurationTest {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Test
    @DisplayName("loads application context and registers route definitions from application.yml")
    void contextLoadsAndRoutesAreRegistered() {
        List<RouteDefinition> definitions = routeDefinitionLocator
                .getRouteDefinitions()
                .collectList()
                .block();

        assertThat(definitions).isNotNull().hasSize(4);

        Set<String> routeIds = definitions.stream()
                .map(RouteDefinition::getId)
                .collect(Collectors.toSet());

        assertThat(routeIds).containsExactlyInAnyOrder(
                "auth-service",
                "ticket-service",
                "file-service",
                "notification-service"
        );
    }

    @Test
    @DisplayName("each route has a non-null URI")
    void eachRouteHasUri() {
        List<RouteDefinition> definitions = routeDefinitionLocator
                .getRouteDefinitions()
                .collectList()
                .block();

        assertThat(definitions).isNotNull();
        for (RouteDefinition definition : definitions) {
            assertThat(definition.getUri()).isNotNull();
            assertThat(definition.getUri().toString())
                    .startsWith("http");
        }
    }
}
