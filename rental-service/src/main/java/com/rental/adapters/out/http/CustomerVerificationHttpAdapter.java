package com.rental.adapters.out.http;

import com.rental.domain.Customer;
import com.rental.domain.CustomerId;
import com.rental.domain.CustomerNotEligibleException;
import com.rental.ports.out.ICustomerVerificationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CustomerVerificationHttpAdapter implements ICustomerVerificationPort {

    private final WebClient webClient;

    public CustomerVerificationHttpAdapter(
            WebClient.Builder webClientBuilder,
            @Value("${rental.customer-service-url:http://localhost:8083}") String customerServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(customerServiceUrl).build();
    }

    @Override
    public Customer findEligibleCustomer(CustomerId customerId) {
        try {
            Boolean canRent = webClient.get()
                    .uri("/api/v1/customers/{id}/can-rent", customerId.getValue())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            if (Boolean.TRUE.equals(canRent)) {
                return Customer.eligible(customerId);
            }
            throw new CustomerNotEligibleException("Customer cannot rent");
        } catch (CustomerNotEligibleException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CustomerNotEligibleException("Unable to verify customer: " + ex.getMessage());
        }
    }

}
