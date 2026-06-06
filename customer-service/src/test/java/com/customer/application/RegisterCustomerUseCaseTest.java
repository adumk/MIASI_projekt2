package com.customer.application;

import com.customer.domain.Customer;
import com.customer.domain.CustomerId;
import com.customer.domain.CustomerRegistered;
import com.customer.domain.CustomerStatus;
import com.customer.domain.DriverLicense;
import com.customer.domain.DuplicateEmailException;
import com.customer.domain.Email;
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
@DisplayName("RegisterCustomerUseCase — customer registration and event publishing")
class RegisterCustomerUseCaseTest {

    @Mock
    private ICustomerRepository customerRepository;

    @Mock
    private IEventPublisher eventPublisher;

    private RegisterCustomerUseCase useCase;

    private RegisterCustomerCommand command;

    @BeforeEach
    void setUp() {
        useCase = new RegisterCustomerUseCase(customerRepository, eventPublisher);
        command = new RegisterCustomerCommand(
                PersonName.of("Jan", "Kowalski"),
                Email.of("jan.kowalski@example.com"),
                DriverLicense.of("DL-12345", LocalDate.now().plusYears(2))
        );
    }

    @Test
    @DisplayName("Should register customer, save and publish CustomerRegistered event")
    void shouldRegisterCustomerAndPublishEvent() {
        // given
        when(customerRepository.existsByEmail(command.email())).thenReturn(false);

        // when
        CustomerId result = useCase.handle(command);

        // then
        assertThat(result).isNotNull();

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(CustomerStatus.ACTIVE);

        verify(eventPublisher).publish(argThat(e -> e instanceof CustomerRegistered));
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException and never save when email already registered")
    void shouldThrowDuplicateEmailExceptionWhenEmailAlreadyRegistered() {
        // given
        when(customerRepository.existsByEmail(command.email())).thenReturn(true);

        // when + then
        assertThatThrownBy(() -> useCase.handle(command))
                .isInstanceOf(DuplicateEmailException.class);

        verify(customerRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }
}