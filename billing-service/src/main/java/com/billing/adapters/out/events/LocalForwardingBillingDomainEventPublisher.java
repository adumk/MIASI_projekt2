package com.billing.adapters.out.events;

import com.billing.domain.DomainEvent;
import com.billing.domain.PaymentConfirmed;
import com.billing.ports.out.IDomainEventPublisher;
import com.billing.ports.out.IInvoiceRepository;
import tools.jackson.databind.ObjectMapper;
import com.rental.events.PaymentConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@Profile("local")
public class LocalForwardingBillingDomainEventPublisher implements IDomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LocalForwardingBillingDomainEventPublisher.class);

    private final LoggingDomainEventPublisherDelegate loggingDelegate;
    private final IInvoiceRepository invoiceRepository;
    private final String rentalBaseUrl;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public LocalForwardingBillingDomainEventPublisher(
            IInvoiceRepository invoiceRepository,
            ObjectMapper objectMapper,
            @Value("${billing.rental-service-url:http://localhost:8081}") String rentalUrl) {
        this.loggingDelegate = new LoggingDomainEventPublisherDelegate();
        this.invoiceRepository = invoiceRepository;
        this.rentalBaseUrl = rentalUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        loggingDelegate.publish(event);
        if (event instanceof PaymentConfirmed confirmed) {
            try {
                String customerId = invoiceRepository
                        .findByRentalId(confirmed.getRentalId())
                        .map(i -> i.getCustomerId().getValue())
                        .orElse("");
                PaymentConfirmedEvent integration = new PaymentConfirmedEvent(
                        confirmed.getRentalId().getValue(),
                        customerId,
                        confirmed.getAmount().toMinorUnits(),
                        confirmed.getAmount().getCurrency());
                String payload = objectMapper.writeValueAsString(integration);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(rentalBaseUrl + "/internal/integration-events"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding());
            } catch (Exception ex) {
                log.warn("Failed to forward PaymentConfirmed: {}", ex.getMessage());
            }
        }
    }
}
