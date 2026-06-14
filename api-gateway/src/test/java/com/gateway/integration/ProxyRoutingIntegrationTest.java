package com.gateway.integration;

import com.gateway.config.GatewayProxyConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyRoutingIntegrationTest {

    private static MockWebServer mockBackend;
    private WebClient webClient;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackend = new MockWebServer();
        mockBackend.start(8081); // Udajemy serwis pod portem 8081
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackend.shutdown();
    }

    @BeforeEach
    void init() {
        webClient = WebClient.builder().build();
    }

    @Test
    void shouldRouteRequestToRentalService() {
        // Przygotuj odpowiedź backendu
        mockBackend.enqueue(new MockResponse()
                .setBody("{\"status\":\"ok\"}")
                .addHeader("Content-Type", "application/json"));

        // Wywołaj metodę proxy z Twojej klasy (wymagałoby to zmiany dostępu na public w configu)
        // LUB po prostu przetestuj, czy Twoje resolveTarget() zwraca dobry adres:

        // Sprawdzenie logiki routingu (najważniejszy element)
        // GatewayProxyConfig.resolveTarget(...) jest private, więc albo zmień na public,
        // albo przetestuj to w testach jednostkowych (zalecane).
    }
}