package com.customer.adapters.in.web;

import com.customer.application.AuthSession;
import com.customer.application.EmailAlreadyRegisteredException;
import com.customer.application.InvalidCredentialsException;
import com.customer.application.LoginCommand;
import com.customer.application.LoginUseCase;
import com.customer.application.RegisterWithPasswordCommand;
import com.customer.application.RegisterWithPasswordUseCase;
import com.customer.domain.Customer;
import com.customer.domain.CustomerId;
import com.customer.infrastructure.security.JwtTokenProvider;
import com.customer.ports.out.ICustomerRepository;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final LoginUseCase loginUseCase;
    private final RegisterWithPasswordUseCase registerUseCase;
    private final ICustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthRestController(
            LoginUseCase loginUseCase,
            RegisterWithPasswordUseCase registerUseCase,
            ICustomerRepository customerRepository,
            JwtTokenProvider jwtTokenProvider) {
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
        this.customerRepository = customerRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthSession session = loginUseCase.handle(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(AuthResponse.from(session));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterAuthRequest request) {
        AuthSession session = registerUseCase.handle(new RegisterWithPasswordCommand(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password(),
                request.licenseNumber(),
                request.licenseExpiryDate()));
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(session));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Claims claims = parseBearer(authorization);
        Customer customer = customerRepository.findById(CustomerId.of(claims.getSubject()));
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String role = claims.get("role", String.class);
        return ResponseEntity.ok(new AuthResponse(
                customer.getCustomerId().getValue(),
                customer.getEmail().getValue(),
                customer.getFullName().getFirstName(),
                customer.getFullName().getLastName(),
                role != null ? role : "CUSTOMER",
                customer.isVerified(),
                null));
    }

    private Claims parseBearer(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new InvalidCredentialsException("Missing bearer token");
        }
        return jwtTokenProvider.parseClaims(authorization.substring(7));
    }
}
