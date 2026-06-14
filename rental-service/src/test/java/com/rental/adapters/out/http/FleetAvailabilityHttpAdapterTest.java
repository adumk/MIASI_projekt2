package com.rental.adapters.out.http;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.VehicleId;
import com.rental.domain.VehicleNotAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.*;
import java.net.http.HttpClient;
import org.springframework.http.client.reactive.JdkClientHttpConnector;

@DisplayName("FleetAvailabilityHttpAdapter — HTTP calls to fleet-service")
class FleetAvailabilityHttpAdapterTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private FleetAvailabilityHttpAdapter adapter;
    private DateRange period;

    @BeforeEach
    void setUp() {
        adapter = new FleetAvailabilityHttpAdapter(
                WebClient.builder()
                        .clientConnector(
                                new JdkClientHttpConnector(
                                        HttpClient.newHttpClient())),
                "http://localhost:" + wireMock.getPort());
        period = DateRange.of(LocalDate.now(), LocalDate.now().plusDays(3));
    }

    @Test
    @DisplayName("Should return true when vehicle status is AVAILABLE")
    void shouldReturnTrueWhenVehicleStatusIsAvailable() {
        // given
        wireMock.stubFor(get(urlPathMatching("/api/v1/vehicles/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"id":"vehicle-001","status":"AVAILABLE","category":"STANDARD"}
                                """)));

        // when
        boolean result = adapter.isAvailable(VehicleId.of("vehicle-001"), period);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when vehicle status is RENTED")
    void shouldReturnFalseWhenVehicleStatusIsRented() {
        // given
        wireMock.stubFor(get(urlPathMatching("/api/v1/vehicles/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"id":"vehicle-001","status":"RENTED","category":"STANDARD"}
                                """)));

        // when
        boolean result = adapter.isAvailable(VehicleId.of("vehicle-001"), period);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should throw VehicleNotAvailableException when service returns 404")
    void shouldThrowVehicleNotAvailableWhenServiceReturns404() {
        // given
        wireMock.stubFor(get(urlPathMatching("/api/v1/vehicles/.*"))
                .willReturn(aResponse()
                        .withStatus(404)));

        // when + then
        assertThatThrownBy(() -> adapter.isAvailable(VehicleId.of("vehicle-001"), period))
                .isInstanceOf(VehicleNotAvailableException.class);
    }

    @Test
    @DisplayName("Should return false when response body is null")
    void shouldReturnFalseWhenResponseBodyIsNull() {
        // given
        wireMock.stubFor(get(urlPathMatching("/api/v1/vehicles/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("null")));

        // when
        boolean result = adapter.isAvailable(VehicleId.of("vehicle-001"), period);

        // then
        assertThat(result).isFalse();
    }
}