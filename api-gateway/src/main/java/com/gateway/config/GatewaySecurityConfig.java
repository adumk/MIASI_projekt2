package com.gateway.config;

import com.gateway.security.GatewayJwtValidator;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "gateway.security.enabled", havingValue = "true", matchIfMissing = true)
public class GatewaySecurityConfig implements WebFilter, Ordered {

    private final GatewayJwtValidator jwtValidator;
    private final Set<String> publicPaths;

    public GatewaySecurityConfig(
            GatewayJwtValidator jwtValidator,
            @Value("${gateway.security.public-paths:/api/v1/auth/login,/api/v1/auth/register}") String publicPathsCsv) {
        this.jwtValidator = jwtValidator;
        this.publicPaths = Arrays.stream(publicPathsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.equals("/") || path.startsWith("/actuator") || isPublic(path)) {
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            jwtValidator.validate(auth.substring(7));
            return chain.filter(exchange);
        } catch (JwtException | IllegalArgumentException e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublic(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
