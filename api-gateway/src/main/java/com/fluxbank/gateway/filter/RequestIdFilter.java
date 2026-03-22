package com.fluxbank.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global filter that injects a unique {@code X-Request-Id} header into every
 * inbound request (if not already present) and echoes it back in the response.
 *
 * <p>The request ID is propagated downstream so that distributed traces can be
 * correlated across service logs.
 */
@Slf4j
@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        final String finalRequestId = requestId;
        log.debug("Routing request {} {} [requestId={}]",
                request.getMethod(), request.getURI(), finalRequestId);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, finalRequestId)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .then(Mono.fromRunnable(() ->
                        exchange.getResponse().getHeaders()
                                .add(REQUEST_ID_HEADER, finalRequestId)));
    }

    @Override
    public int getOrder() {
        // Run before all other filters
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
