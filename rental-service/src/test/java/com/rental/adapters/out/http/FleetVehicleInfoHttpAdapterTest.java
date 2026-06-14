package com.rental.adapters.out.http;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.rental.domain.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.*;
import java.net.http.HttpClient;
import org.springframework.http.client.reactive.JdkClientHttpConnector;

@DisplayName("FleetVehicleInfoHttpAdapter — HTTP calls to fleet-service for vehicle info")
class FleetVehicleInfoHttpAdapterTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private FleetVehicleInfoHttpAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FleetVehicleInfoHttpAdapter(
                WebClient.builder()
                        .clientConnector(
                                new JdkClientHttpConnector(
                                        HttpClient.newHttpClient())),
                "http://localhost:" + wireMock.getPort());
    }

    @Test
    @DisplayName("Should resolve category from fleet service")
    void shouldResolveCategoryFromFleetService() {
        // given
        wireMock.stubFor(get(urlPathMatching("/api/v1/vehicles/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"id":"vehicle-001","status":"AVAILABLE","category":"PREMIUM"}
                                """)));

        // when
        String category = adapter.resolveCategory(VehicleId.of("vehicle-001"));

        // then
        assertThat(category).isEqualTo("PREMIUM");
    }

    @Test
    @DisplayName("Should return fallback category when vehicle not found")
    void shouldHandleVehicleNotFoundFromFleetService() {
        // given
        wireMock.stubFor(get(urlPathMatching("/api/v1/vehicles/.*"))
                .willReturn(aResponse()
                        .withStatus(404)));

        // when + then
        // adapter rzuca wyjątek przy błędzie HTTP lub zwraca fallback — nigdy NPE
        assertThatCode(() -> {
            String category = adapter.resolveCategory(VehicleId.of("nonexistent"));
            // jeśli nie rzuca — powinien zwrócić fallback
            assertThat(category).isNotNull();
        }).doesNotThrowAnyException();
    }
}