package com.billing.adapters.in.kafka;

import com.billing.domain.Invoice;
import com.billing.domain.RentalId;
import com.billing.ports.out.IInvoiceRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RentalCancelledKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(RentalCancelledKafkaListener.class);

    private final ObjectMapper objectMapper;
    private final IInvoiceRepository invoiceRepository;

    public RentalCancelledKafkaListener(ObjectMapper objectMapper, IInvoiceRepository invoiceRepository) {
        this.objectMapper = objectMapper;
        this.invoiceRepository = invoiceRepository;
    }

    public void onRentalCancelled(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            if (!"RentalCancelled".equals(root.path("eventType").asText())) {
                return;
            }
            RentalId rentalId = RentalId.of(root.path("rentalId").asText());
            invoiceRepository.findByRentalId(rentalId).ifPresent(invoice -> {
                if (invoice.isPaid()) {
                    invoice.issueRefund();
                    invoiceRepository.save(invoice);
                    log.info("RefundIssued for cancelled rental {}", rentalId.getValue());
                }
            });
        } catch (Exception ex) {
            log.error("Failed to process RentalCancelled for billing", ex);
        }
    }
}
