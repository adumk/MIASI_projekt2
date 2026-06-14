package com.gateway.config;

import com.gateway.security.GatewayJwtValidator;
import io.jsonwebtoken.Claims;
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
class GatewayRoleFilterTest {

    private GatewayRoleFilter roleFilter;

    @Mock
    private GatewayJwtValidator jwtValidator;

    @Mock
    private WebFilterChain filterChain;

    @Mock
    private Claims claims;

    private final String publicPathsCsv = "/api/v1/auth/login,/api/v1/auth/register";

    @BeforeEach
    void setUp() {
        // Tworzymy filtr ręcznie, przekazując zamakowany walidator oraz listę publicznych ścieżek
        roleFilter = new GatewayRoleFilter(jwtValidator, publicPathsCsv);

        // Domyślnie ustawiamy, że chain.filter() zwraca pusty strumień Mono.empty() (sukces przejścia dalej)
        lenient().when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldPassPublicPathWithoutToken() {
        // given - żądanie na publiczną ścieżkę logowania
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/auth/login").build()
        );

        // when & then - w świecie reaktywnym używamy StepVerifier do sprawdzenia wyniku Mono
        StepVerifier.create(roleFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Sprawdzamy, czy filtr pozwolił żądaniu przejść dalej do kolejnego ogniwa
        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void shouldPassToNextFilterWhenNoTokenProvided() {
        // given - ścieżka prywatna, ale brak nagłówka Authorization
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/vehicles").build()
        );

        // when & then
        StepVerifier.create(roleFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Twój kod puszcza to żądanie dalej (zgodnie z if (auth == null))
        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void shouldReturn401ForInvalidToken() {
        // given - błędny token w nagłówku
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/vehicles")
                        .header("Authorization", "Bearer zly-token")
                        .build()
        );
        // Konfigurujemy mock walidatora, aby rzucił wyjątek JwtException
        when(jwtValidator.validate("zly-token")).thenThrow(new JwtException("Invalid token"));

        // when & then
        StepVerifier.create(roleFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Sprawdzamy, czy filtr przerwał łańcuch i ustawił status 401 UNAUTHORIZED
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(exchange);
    }

    @Test
    void shouldAllowAdminEverywhere() {
        // given - żądanie na chronioną ścieżkę administratora z tokenem ADMINA
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/admin/users")
                        .header("Authorization", "Bearer token-admina")
                        .build()
        );
        when(jwtValidator.validate("token-admina")).thenReturn(claims);
        when(claims.get("role", String.class)).thenReturn("ADMIN");

        // when & then
        StepVerifier.create(roleFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Admin powinien zostać przepuszczony
        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void shouldRejectCustomerFromAdminPath() {
        // given - żądanie klienta (CUSTOMER) na ścieżkę admina
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/admin/users")
                        .header("Authorization", "Bearer token-klienta")
                        .build()
        );
        when(jwtValidator.validate("token-klienta")).thenReturn(claims);
        when(claims.get("role", String.class)).thenReturn("CUSTOMER");

        // when & then
        StepVerifier.create(roleFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Sprawdzamy, czy klient dostał 403 FORBIDDEN i został zablokowany
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(exchange);
    }

    @Test
    void shouldAllowEmployeeToPostVehicles() {
        // given - Pracownik (EMPLOYEE) próbuje dodać nowy pojazd (POST /api/v1/vehicles)
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/vehicles")
                        .header("Authorization", "Bearer token-pracownika")
                        .build()
        );
        when(jwtValidator.validate("token-pracownika")).thenReturn(claims);
        when(claims.get("role", String.class)).thenReturn("EMPLOYEE");

        // when & then
        StepVerifier.create(roleFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Pracownik ma uprawnienia do tworzenia pojazdów, więc przechodzi
        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void shouldRejectCustomerFromPostVehicles() {
        // given - Zwykły klient (CUSTOMER) próbuje dodać pojazd (POST /api/v1/vehicles)
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/vehicles")
                        .header("Authorization", "Bearer token-klienta")
                        .build()
        );
        when(jwtValidator.validate("token-klienta")).thenReturn(claims);
        when(claims.get("role", String.class)).thenReturn("CUSTOMER");

        // when & then
        StepVerifier.create(roleFilter.filter(exchange, filterChain))
                .verifyComplete();

        // Klient nie może dodawać aut -> 403 FORBIDDEN
        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(exchange);
    }
}