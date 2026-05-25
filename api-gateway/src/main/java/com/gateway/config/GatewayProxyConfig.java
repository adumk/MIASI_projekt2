package com.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

@Configuration
public class GatewayProxyConfig {

    private static final List<RouteRule> RULES = List.of(
            new RouteRule("/api/v1/auth", "http://localhost:8083"),
            new RouteRule("/api/v1/availability", "http://localhost:8081"),
            new RouteRule("/api/v1/reservations", "http://localhost:8081"),
            new RouteRule("/api/v1/rentals", "http://localhost:8081"),
            new RouteRule("/api/v1/admin", "http://localhost:8081"),
            new RouteRule("/api/v1/vehicles", "http://localhost:8082"),
            new RouteRule("/api/v1/customers", "http://localhost:8083"),
            new RouteRule("/api/v1/invoices", "http://localhost:8084"),
            new RouteRule("/api/v1/payments", "http://localhost:8084"),
            new RouteRule("/api/v1/tariffs", "http://localhost:8084"),
            new RouteRule("/api/v1/quotes", "http://localhost:8084")
    );

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes(WebClient.Builder webClientBuilder) {
        WebClient client = webClientBuilder.build();
        return RouterFunctions.route(path("/api/**"), request -> proxy(client, request))
                .andRoute(GET("/api"), request ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue("""
                                        {"message":"API Gateway","basePath":"/api/v1","examples":[
                                          "/api/v1/vehicles/vehicle-001",
                                          "/api/v1/customers/customer-001"
                                        ]}"""));
    }

    private static Mono<ServerResponse> proxy(WebClient client, ServerRequest request) {
        String targetBase = resolveTarget(request.path());

        if (targetBase == null) {
            return ServerResponse.notFound().build();
        }

        URI targetUri = URI.create(targetBase + request.uri().getRawPath()
                + (request.uri().getRawQuery() != null ? "?" + request.uri().getRawQuery() : ""));

        WebClient.RequestBodySpec spec = client.method(HttpMethod.valueOf(request.method().name()))
                .uri(targetUri);

        request.headers().asHttpHeaders().forEach((name, values) -> {
            if (!isHopByHop(name)) {
                values.forEach(value -> spec.header(name, value));
            }
        });

        return request.bodyToMono(byte[].class)
                .defaultIfEmpty(new byte[0])
                .flatMap(body -> {
                    WebClient.RequestHeadersSpec<?> outbound = body.length > 0
                            ? spec.contentType(MediaType.APPLICATION_JSON).bodyValue(body)
                            : spec;
                    return outbound.exchangeToMono(response -> response.bodyToMono(byte[].class)
                            .defaultIfEmpty(new byte[0])
                            .flatMap(responseBody -> {
                                HttpHeaders headers = filterResponseHeaders(response.headers().asHttpHeaders());
                                headers.remove(HttpHeaders.CONTENT_LENGTH);
                                headers.remove(HttpHeaders.TRANSFER_ENCODING);
                                MediaType contentType = response.headers().contentType().orElse(null);
                                ServerResponse.BodyBuilder builder = ServerResponse.status(response.statusCode());
                                if (contentType != null) {
                                    builder.contentType(contentType);
                                }
                                return builder.headers(h -> h.addAll(headers)).bodyValue(responseBody);
                            }));
                });
    }

    private static HttpHeaders filterResponseHeaders(HttpHeaders headers) {
        HttpHeaders out = new HttpHeaders();
        headers.forEach((name, values) -> {
            if (!isHopByHop(name)) {
                out.addAll(name, values);
            }
        });
        return out;
    }

    private static boolean isHopByHop(String header) {
        return Map.of(
                HttpHeaders.HOST, true,
                HttpHeaders.CONNECTION, true,
                HttpHeaders.TRANSFER_ENCODING, true,
                "Keep-Alive", true,
                "Proxy-Authenticate", true,
                "Proxy-Authorization", true,
                "TE", true,
                "Trailer", true,
                "Upgrade", true
        ).containsKey(header);
    }

    private static String resolveTarget(String path) {
        if (path.matches("/api/v1/customers/[^/]+/rentals.*")) {
            return "http://localhost:8081";
        }
        return RULES.stream()
                .filter(rule -> path.startsWith(rule.prefix()))
                .map(RouteRule::target)
                .findFirst()
                .orElse(null);
    }

    private record RouteRule(String prefix, String target) {}
}
