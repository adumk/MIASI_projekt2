package com.gateway.config;

import com.gateway.security.GatewayJwtValidator;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewaySecurityConfigTest {

    private GatewaySecurityConfig securityConfig;

    @Mock
    private GatewayJwtValidator jwtValidator;

    @Mock
    private WebFilterChain filterChain;

    private final String publicPathsCsv = "/api/v1/auth/login,/api/v1/auth/register";

    @BeforeEach
    void setUp() {
        // Ręczna inicjalizacja z zamakowanymi zależnościami
        securityConfig = new GatewaySecurityConfig(jwtValidator, publicPathsCsv);

        // Zapewniamy, że przejście do kolejnego filtra domyślnie zwraca pusty strumień reaktywny
        lenient().when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowRootPathWithoutToken() {
        // given - żądanie na czystą ścieżkę główną "/"
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/").build()
        );

        // when & then
        StepVerifier.create(securityConfig.filter(exchange, filterChain))
                .verifyComplete();

        // Powinno przepuścić bez sprawdzania tokenu
        verify(filterChain, times(1)).filter(exchange);
        verifyNoInteractions(jwtValidator);
    }

    @Test
    void shouldAllowActuatorPathWithoutToken() {
        // given - żądanie na punkty kontrolne Actuatora
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/actuator/health").build()
        );

        // when & then
        StepVerifier.create(securityConfig.filter(exchange, filterChain))
                .verifyComplete();

        // Actuator ma być ogólnodostępny
        verify(filterChain, times(1)).filter(exchange);
        verifyNoInteractions(jwtValidator);
    }

    @Test
    void shouldAllowPublicPathWithoutToken() {
        // given - żądanie na ścieżkę rejestracji (zdefiniowaną w public-paths)
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/auth/register").build()
        );

        // when & then
        StepVerifier.create(securityConfig.filter(exchange, filterChain))
                .verifyComplete();

        // Rejestracja musi być dostępna bez tokenu
        verify(filterChain, times(1)).filter(exchange);
        verifyNoInteractions(jwtValidator);
    }

    @Test
    void shouldReturn401WhenNoTokenProvidedOnProtectedPath() {
        // given - próba wejścia na chronioną ścieżkę bez nagłówka Authorization
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/vehicles").build()
        );

        // when & then
        StepVerifier.create(securityConfig.filter(exchange, filterChain))
                .verifyComplete();

        // Powinno natychmiast ubić żądanie i zwrócić status 401 UNAUTHORIZED
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(exchange);
    }

    @Test
    void shouldReturn401WhenTokenDoesNotStartWithBearer() {
        // given - nagłówek jest, ale ma zły format (np. Basic zamiast Bearer)
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/vehicles")
                        .header("Authorization", "Basic dXNlcjpwYXNz")
                        .build()
        );

        // when & then
        StepVerifier.create(securityConfig.filter(exchange, filterChain))
                .verifyComplete();

        // Niepoprawny schemat autoryzacji -> 401
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(exchange);
    }

    @Test
    void shouldAllowValidTokenOnProtectedPath() {
        // given - poprawne żądanie z poprawnym tokenem
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/vehicles")
                        .header("Authorization", "Bearer poprawny-token-jwt")
                        .build()
        );
        // Walidator nie rzuca błędu, co oznacza sukces walidacji
        when(jwtValidator.validate("poprawny-token-jwt")).thenReturn(null);

        // when & then
        StepVerifier.create(securityConfig.filter(exchange, filterChain))
                .verifyComplete();

        // Token poprawny -> przekazujemy żądanie dalej w łańcuchu filtrów
        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void shouldReturn401ForInvalidTokenOnProtectedPath() {
        // given - token przekazany, ale sfałszowany/wygasły
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/vehicles")
                        .header("Authorization", "Bearer zepsuty-token")
                        .build()
        );
        // Konfigurujemy mock, by rzucił wyjątek JwtException
        when(jwtValidator.validate("zepsuty-token")).thenThrow(new JwtException("Token expired or invalid"));

        // when & then
        StepVerifier.create(securityConfig.filter(exchange, filterChain))
                .verifyComplete();

        // Zły token -> 401 UNAUTHORIZED
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(exchange);
    }
}