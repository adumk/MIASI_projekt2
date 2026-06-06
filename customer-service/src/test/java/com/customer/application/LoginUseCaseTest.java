package com.customer.application;

import com.customer.domain.Customer;
import com.customer.domain.CustomerId;
import com.customer.domain.DriverLicense;
import com.customer.domain.Email;
import com.customer.domain.PersonName;
import com.customer.infrastructure.security.JwtTokenProvider;
import com.customer.ports.out.CustomerAuthView;
import com.customer.ports.out.ICustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase — authentication and token generation")
class LoginUseCaseTest {

    @Mock
    private ICustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private LoginUseCase useCase;

    private Customer activeCustomer;
    private Email email;

    @BeforeEach
    void setUp() {
        useCase = new LoginUseCase(customerRepository, passwordEncoder, jwtTokenProvider);
        email = Email.of("jan@example.com");
        activeCustomer = Customer.create(
                CustomerId.of("customer-001"),
                PersonName.of("Jan", "Kowalski"),
                email,
                DriverLicense.of("DL-001", LocalDate.now().plusYears(1))
        );
        activeCustomer.register();
        activeCustomer.clearDomainEvents();
    }

    @Test
    @DisplayName("Should return AuthSession with token when credentials are valid")
    void shouldReturnAuthSessionWithTokenWhenCredentialsAreValid() {
        // given
        CustomerAuthView authView = new CustomerAuthView(activeCustomer, "hashed_pass", "CUSTOMER");
        when(customerRepository.findAuthByEmail(email)).thenReturn(authView);
        when(passwordEncoder.matches("rawpassword", "hashed_pass")).thenReturn(true);
        when(jwtTokenProvider.createToken(
                activeCustomer.getCustomerId().getValue(),
                email.getValue(),
                "CUSTOMER")).thenReturn("jwt.token.here");

        // when
        AuthSession session = useCase.handle(new LoginCommand("jan@example.com", "rawpassword"));

        // then
        assertThat(session.token()).isEqualTo("jwt.token.here");
        assertThat(session.role()).isEqualTo("CUSTOMER");
        assertThat(session.customerId()).isEqualTo("customer-001");
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when email not found")
    void shouldThrowInvalidCredentialsWhenEmailNotFound() {
        // given
        when(customerRepository.findAuthByEmail(email)).thenReturn(null);

        // when + then
        assertThatThrownBy(() -> useCase.handle(new LoginCommand("jan@example.com", "rawpassword")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password does not match")
    void shouldThrowInvalidCredentialsWhenPasswordDoesNotMatch() {
        // given
        CustomerAuthView authView = new CustomerAuthView(activeCustomer, "hashed_pass", "CUSTOMER");
        when(customerRepository.findAuthByEmail(email)).thenReturn(authView);
        when(passwordEncoder.matches("wrongpassword", "hashed_pass")).thenReturn(false);

        // when + then
        assertThatThrownBy(() -> useCase.handle(new LoginCommand("jan@example.com", "wrongpassword")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password hash is null")
    void shouldThrowInvalidCredentialsWhenPasswordHashIsNull() {
        // given
        CustomerAuthView authView = new CustomerAuthView(activeCustomer, null, "CUSTOMER");
        when(customerRepository.findAuthByEmail(email)).thenReturn(authView);

        // when + then
        assertThatThrownBy(() -> useCase.handle(new LoginCommand("jan@example.com", "rawpassword")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(any(), any());
    }
}