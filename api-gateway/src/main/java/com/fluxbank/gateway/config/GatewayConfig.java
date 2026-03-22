package com.fluxbank.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Gateway configuration.
 *
 * <p>Routes (with circuit breakers, predicates, and per-route filters) are declared
 * entirely in {@code application.yml} to keep routing rules visible and diff-friendly.
 * This class only wires infrastructure beans that cannot be expressed in YAML.
 */
@Slf4j
@Configuration
public class GatewayConfig {

    /**
     * Fallback handler — returns a 503 JSON response for each downstream service
     * when its circuit breaker trips open.
     */
    @Bean
    public RouterFunction<ServerResponse> fallbackRoutes() {
        return RouterFunctions.route()
                .GET("/fallback/{service}", request -> buildFallback(request.pathVariable("service")))
                .POST("/fallback/{service}", request -> buildFallback(request.pathVariable("service")))
                .PUT("/fallback/{service}", request -> buildFallback(request.pathVariable("service")))
                .DELETE("/fallback/{service}", request -> buildFallback(request.pathVariable("service")))
                .PATCH("/fallback/{service}", request -> buildFallback(request.pathVariable("service")))
                .build();
    }

    private static reactor.core.publisher.Mono<ServerResponse> buildFallback(String service) {
        log.warn("Circuit breaker open — service unavailable: {}", service);
        String body = """
                {"success":false,"error":{"code":"SYSTEM_001",\
                "message":"Service '%s' is temporarily unavailable. Please retry in a few seconds.",\
                "details":[]}}""".formatted(service);
        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body);
    }
}

