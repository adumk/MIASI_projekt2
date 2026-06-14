package com.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class GatewayProxyConfigTest {

    private WebTestClient webTestClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @BeforeEach
    void setUp() {
        // Gwarantujemy, że metody wewnętrzne Buildera zwracają samych siebie (zabezpieczenie przed NullPointerException)
        lenient().when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.defaultHeader(anyString(), any())).thenReturn(webClientBuilder);
        lenient().when(webClientBuilder.filter(any())).thenReturn(webClientBuilder);

        // Na koniec budowania zwracamy zamakowany WebClient
        lenient().when(webClientBuilder.build()).thenReturn(webClient);

        // Inicjalizacja konfiguracji i bindowanie routera
        GatewayProxyConfig config = new GatewayProxyConfig();
        RouterFunction<ServerResponse> routes = config.gatewayRoutes(webClientBuilder);
        webTestClient = WebTestClient.bindToRouterFunction(routes).build();
    }

    @Test
    void shouldReturn404WhenPathDoesNotMatchAnyRule() {
        webTestClient.get().uri("/api/v1/unknown-service/data")
                .exchange()
                .expectStatus().isNotFound();

        verifyNoInteractions(webClient);
    }
}