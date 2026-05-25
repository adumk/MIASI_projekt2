package com.customer.infrastructure;

import com.customer.application.BlockCustomerUseCase;
import com.customer.application.GetCustomerUseCase;
import com.customer.application.RegisterCustomerUseCase;
import com.customer.application.VerifyCustomerUseCase;
import com.customer.ports.out.ICustomerRepository;
import com.customer.ports.out.IEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerServiceConfig {

    @Bean
    RegisterCustomerUseCase registerCustomerUseCase(
            ICustomerRepository customerRepository, IEventPublisher eventPublisher) {
        return new RegisterCustomerUseCase(customerRepository, eventPublisher);
    }

    @Bean
    VerifyCustomerUseCase verifyCustomerUseCase(
            ICustomerRepository customerRepository, IEventPublisher eventPublisher) {
        return new VerifyCustomerUseCase(customerRepository, eventPublisher);
    }

    @Bean
    BlockCustomerUseCase blockCustomerUseCase(ICustomerRepository customerRepository) {
        return new BlockCustomerUseCase(customerRepository);
    }

    @Bean
    GetCustomerUseCase getCustomerUseCase(ICustomerRepository customerRepository) {
        return new GetCustomerUseCase(customerRepository);
    }
}
