package com.customer.application;

import com.customer.domain.Email;
import com.customer.infrastructure.security.JwtTokenProvider;
import com.customer.ports.out.CustomerAuthView;
import com.customer.ports.out.ICustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCase {

    private final ICustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginUseCase(
            ICustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthSession handle(LoginCommand command) {
        CustomerAuthView auth = customerRepository.findAuthByEmail(Email.of(command.email()));
        if (auth == null || auth.passwordHash() == null) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (!passwordEncoder.matches(command.password(), auth.passwordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        String token = jwtTokenProvider.createToken(
                auth.customer().getCustomerId().getValue(),
                auth.customer().getEmail().getValue(),
                auth.role());
        return AuthSession.from(auth.customer(), auth.role(), token);
    }
}
