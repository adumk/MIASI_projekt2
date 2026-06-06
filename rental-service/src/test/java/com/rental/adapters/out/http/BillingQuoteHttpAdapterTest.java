package com.rental.adapters.out.http;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.rental.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.*;

@DisplayName("BillingQuoteHttpAdapter — HTTP calls to billing-service for rental quotes")
class BillingQuoteHttpAdapterTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private BillingQuoteHttpAdapter adapter;

    private final LocalDate startDate = LocalDate.of(2026, 6, 1);
    private final LocalDate endDate   = LocalDate.of(2026, 6, 8);

    @BeforeEach
    void setUp() {
        adapter = new BillingQuoteHttpAdapter(
                WebClient.builder(),
                "http://localhost:" + wireMock.getPort());
    }

    @Test
    @DisplayName("Should return quoted cost from billing service")
    void shouldReturnQuotedCostFromBillingService() {
        // given
        wireMock.stubFor(get(urlPathEqualTo("/api/v1/quotes"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "category": "STANDARD",
                                  "rentalDays": 7,
                                  "dailyRateMinorUnits": 15000,
                                  "totalMinorUnits": 35000,
                                  "currency": "PLN"
                                }
                                """)));

        // when
        Money result = adapter.quoteRentalCost("STANDARD", startDate, endDate);

        // then
        assertThat(result).isEqualTo(Money.of(35000, "PLN"));
    }

    @Test
    @DisplayName("Should throw exception when billing service is unavailable")
    void shouldHandleBillingServiceUnavailability() {
        // given
        wireMock.stubFor(get(urlPathEqualTo("/api/v1/quotes"))
                .willReturn(aResponse()
                        .withStatus(503)));

        // when + then
        assertThatThrownBy(() -> adapter.quoteRentalCost("STANDARD", startDate, endDate))
                .isInstanceOf(Exception.class);
    }
}