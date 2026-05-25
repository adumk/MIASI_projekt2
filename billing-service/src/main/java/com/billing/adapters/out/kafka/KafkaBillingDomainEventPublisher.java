package com.billing.adapters.out.kafka;

import com.billing.domain.DomainEvent;
import com.billing.adapters.out.events.LoggingDomainEventPublisherDelegate;
import com.billing.domain.PaymentConfirmed;
import com.billing.ports.out.IDomainEventPublisher;
import com.billing.ports.out.IInvoiceRepository;
import tools.jackson.databind.ObjectMapper;
import com.rental.events.EventTopics;
import com.rental.events.PaymentConfirmedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(KafkaTemplate.class)
public class KafkaBillingDomainEventPublisher implements IDomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaBillingDomainEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final LoggingDomainEventPublisherDelegate loggingDelegate;
    private final IInvoiceRepository invoiceRepository;

    public KafkaBillingDomainEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            IInvoiceRepository invoiceRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.loggingDelegate = new LoggingDomainEventPublisherDelegate();
        this.invoiceRepository = invoiceRepository;
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
                kafkaTemplate.send(
                        EventTopics.BILLING_EVENTS,
                        objectMapper.writeValueAsString(integration));
                log.info("Published PaymentConfirmed to {}", EventTopics.BILLING_EVENTS);
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to publish PaymentConfirmed", ex);
            }
        }
    }
}
