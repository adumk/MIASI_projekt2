package com.rental.adapters.out.http;

import com.rental.domain.Money;
import com.rental.ports.out.IBillingQuotePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Map;

@Component
public class BillingQuoteHttpAdapter implements IBillingQuotePort {

    private final WebClient webClient;

    public BillingQuoteHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${rental.billing-service-url:http://localhost:8084}") String billingServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(billingServiceUrl).build();
    }

    @Override
    public Money quoteRentalCost(String vehicleCategory, LocalDate startDate, LocalDate endDate) {
        @SuppressWarnings("unchecked")
        Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/quotes")
                        .queryParam("category", vehicleCategory)
                        .queryParam("startDate", startDate.toString())
                        .queryParam("endDate", endDate.toString())
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        if (response == null) {
            throw new IllegalStateException("Empty quote response from billing service");
        }
        long minor = ((Number) response.get("totalMinorUnits")).longValue();
        String currency = String.valueOf(response.getOrDefault("currency", "PLN"));
        return Money.of(minor, currency);
    }
}
