package com.rental.adapters.out.http;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.rental.domain.Customer;
import com.rental.domain.CustomerId;
import com.rental.domain.CustomerNotEligibleException;
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

@DisplayName("CustomerVerificationHttpAdapter — HTTP calls to customer-service")
class CustomerVerificationHttpAdapterTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private CustomerVerificationHttpAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CustomerVerificationHttpAdapter(
                WebClient.builder()
                        .clientConnector(
                                new JdkClientHttpConnector(
                                        HttpClient.newHttpClient())),
                "http://localhost:" + wireMock.getPort());
    }

    @Test
    @DisplayName("Should return eligible customer when service returns true")
    void shouldReturnEligibleCustomerWhenServiceReturnsTrue() {
        // given
        wireMock.stubFor(get(urlPathMatching("/api/v1/customers/.*/can-rent"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("true")));

        // when
        Customer result = adapter.findEligibleCustomer(CustomerId.of("customer-001"));

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should throw CustomerNotEligibleException when service returns false")
    void shouldThrowCustomerNotEligibleWhenServiceReturnsFalse() {
        // given
        wireMock.stubFor(get(urlPathMatching("/api/v1/customers/.*/can-rent"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("false")));

        // when + then
        assertThatThrownBy(() -> adapter.findEligibleCustomer(CustomerId.of("customer-001")))
                .isInstanceOf(CustomerNotEligibleException.class);
    }

    @Test
    @DisplayName("Should throw CustomerNotEligibleException when service returns 404")
    void shouldThrowCustomerNotEligibleWhenServiceReturns404() {
        // given
        wireMock.stubFor(get(urlPathMatching("/api/v1/customers/.*/can-rent"))
                .willReturn(aResponse()
                        .withStatus(404)));

        // when + then
        assertThatThrownBy(() -> adapter.findEligibleCustomer(CustomerId.of("customer-001")))
                .isInstanceOf(CustomerNotEligibleException.class);
    }

    @Test
    @DisplayName("Should throw CustomerNotEligibleException when service is unavailable")
    void shouldThrowCustomerNotEligibleWhenServiceIsUnavailable() {
        // given
        wireMock.stubFor(get(urlPathMatching("/api/v1/customers/.*/can-rent"))
                .willReturn(aResponse()
                        .withStatus(503)));

        // when + then
        assertThatThrownBy(() -> adapter.findEligibleCustomer(CustomerId.of("customer-001")))
                .isInstanceOf(CustomerNotEligibleException.class);
    }
}