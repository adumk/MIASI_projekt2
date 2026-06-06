package com.customer.application;

import com.customer.domain.Customer;
import com.customer.domain.CustomerId;
import com.customer.domain.CustomerNotFoundException;
import com.customer.domain.CustomerStatus;
import com.customer.domain.DriverLicense;
import com.customer.domain.Email;
import com.customer.domain.InvalidStatusTransitionException;
import com.customer.domain.PersonName;
import com.customer.ports.out.ICustomerRepository;
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
@DisplayName("BlockCustomerUseCase — blocking customers with reason")
class BlockCustomerUseCaseTest {

    @Mock
    private ICustomerRepository customerRepository;

    private BlockCustomerUseCase useCase;

    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        useCase = new BlockCustomerUseCase(customerRepository);
        customerId = CustomerId.of("customer-001");
    }

    private Customer activeCustomer() {
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
    @DisplayName("Should block active customer with reason and save")
    void shouldBlockActiveCustomerWithReason() {
        // given
        Customer customer = activeCustomer();
        when(customerRepository.findById(customerId)).thenReturn(customer);

        // when
        useCase.handle(new BlockCustomerCommand(customerId, "unpaid invoices"));

        // then
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(CustomerStatus.BLOCKED);
        assertThat(captor.getValue().getBlockReason()).isEqualTo("unpaid invoices");
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when customer does not exist")
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        // given
        when(customerRepository.findById(customerId)).thenReturn(null);

        // when + then
        assertThatThrownBy(() -> useCase.handle(new BlockCustomerCommand(customerId, "fraud")))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidStatusTransitionException when blocking an already blocked customer")
    void shouldThrowWhenBlockingAlreadyBlockedCustomer() {
        // given
        Customer customer = activeCustomer();
        customer.block("first reason");
        when(customerRepository.findById(customerId)).thenReturn(customer);

        // when + then
        assertThatThrownBy(() -> useCase.handle(new BlockCustomerCommand(customerId, "second reason")))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }
}