package com.customer.application;

import com.customer.domain.Customer;
import com.customer.domain.CustomerId;
import com.customer.domain.DriverLicense;
import com.customer.domain.Email;
import com.customer.domain.PersonName;
import com.customer.infrastructure.security.JwtTokenProvider;
import com.customer.ports.out.ICustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterWithPasswordUseCase {

    private final ICustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public RegisterWithPasswordUseCase(
            ICustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthSession handle(RegisterWithPasswordCommand command) {
        Email email = Email.of(command.email());
        if (customerRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException("Email is already registered");
        }
        Customer customer = Customer.create(
                CustomerId.generate(),
                PersonName.of(command.firstName(), command.lastName()),
                email,
                DriverLicense.of(command.licenseNumber(), command.licenseExpiryDate()));
        customer.register();
        String hash = passwordEncoder.encode(command.password());
        customerRepository.saveWithPassword(customer, hash, "CUSTOMER");
        String token = jwtTokenProvider.createToken(
                customer.getCustomerId().getValue(),
                email.getValue(),
                "CUSTOMER");
        return AuthSession.from(customer, "CUSTOMER", token);
    }
}
