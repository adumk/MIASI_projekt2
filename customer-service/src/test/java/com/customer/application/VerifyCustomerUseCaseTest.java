package com.customer.application;

import com.customer.domain.Customer;
import com.customer.domain.CustomerId;
import com.customer.domain.CustomerNotFoundException;
import com.customer.domain.CustomerVerified;
import com.customer.domain.DriverLicense;
import com.customer.domain.Email;
import com.customer.domain.InvalidStatusTransitionException;
import com.customer.domain.PersonName;
import com.customer.ports.out.ICustomerRepository;
import com.customer.ports.out.IEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerifyCustomerUseCase — customer verification and event publishing")
class VerifyCustomerUseCaseTest {

    @Mock
    private ICustomerRepository customerRepository;

    @Mock
    private IEventPublisher eventPublisher;

    private VerifyCustomerUseCase useCase;

    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        useCase = new VerifyCustomerUseCase(customerRepository, eventPublisher);
        customerId = CustomerId.of("customer-001");
    }

    private Customer activeUnverifiedCustomer() {
        Customer customer = Customer.create(
                customerId,
                PersonName.of("Jan", "Kowalski"),
                Email.of("jan@example.com"),
                DriverLicense.of("DL-001", LocalDate.now().plusYears(1))
        );
        customer.register();
        customer.clearDomainEvents();
        return customer;
    }

    @Test
    @DisplayName("Should verify customer, save and publish CustomerVerified event")
    void shouldVerifyCustomerAndPublishEvent() {
        // given
        Customer customer = activeUnverifiedCustomer();
        when(customerRepository.findById(customerId)).thenReturn(customer);

        // when
        useCase.handle(new VerifyCustomerCommand(customerId));

        // then
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().isVerified()).isTrue();

        verify(eventPublisher).publish(argThat(e -> e instanceof CustomerVerified));
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when customer does not exist")
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        // given
        when(customerRepository.findById(customerId)).thenReturn(null);

        // when + then
        assertThatThrownBy(() -> useCase.handle(new VerifyCustomerCommand(customerId)))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when customer is already verified")
    void shouldThrowWhenCustomerAlreadyVerified() {
        // given
        Customer customer = activeUnverifiedCustomer();
        customer.verify();
        customer.clearDomainEvents();
        when(customerRepository.findById(customerId)).thenReturn(customer);

        // when + then
        assertThatThrownBy(() -> useCase.handle(new VerifyCustomerCommand(customerId)))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }
}