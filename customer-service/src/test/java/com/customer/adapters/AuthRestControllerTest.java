package com.customer.adapters;

import com.customer.adapters.in.web.AuthRestController;
import com.customer.application.AuthSession;
import com.customer.application.InvalidCredentialsException;
import com.customer.application.LoginUseCase;
import com.customer.application.RegisterWithPasswordUseCase;
import com.customer.infrastructure.security.JwtTokenProvider;
import com.customer.ports.out.ICustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthRestController — login endpoint HTTP contract")
class AuthRestControllerTest {

    private MockMvc mockMvc;

    @Mock private LoginUseCase loginUseCase;
    @Mock private RegisterWithPasswordUseCase registerWithPasswordUseCase;
    @Mock private ICustomerRepository customerRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthRestController(
                loginUseCase, registerWithPasswordUseCase,
                customerRepository, jwtTokenProvider)).build();
    }

    private AuthSession stubSession() {
        return new AuthSession(
                "customer-001",
                "jan@example.com",
                "Jan",
                "Kowalski",
                "CUSTOMER",
                true,
                "jwt.token.here");
    }

    @Test
    @DisplayName("Should return 200 with token when login successful")
    void shouldReturn200WithTokenWhenLoginSuccessful() throws Exception {
        // given
        when(loginUseCase.handle(any())).thenReturn(stubSession());

        String payload = """
                {"email":"jan@example.com","password":"secret123"}
                """;

        // when + then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt.token.here"))
                .andExpect(jsonPath("$.customerId").value("customer-001"));
    }

    @Test
    @DisplayName("Should return 401 when credentials are invalid")
    void shouldReturn401WhenInvalidCredentials() throws Exception {
        // given
        when(loginUseCase.handle(any()))
                .thenThrow(new InvalidCredentialsException("Bad credentials"));

        String payload = """
                {"email":"jan@example.com","password":"wrongpassword"}
                """;

        // when + then
        assertThrows(Exception.class, () ->
                mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
        );
    }

    @Test
    @DisplayName("Should return 400 when login request is invalid")
    void shouldReturn400WhenLoginRequestInvalid() throws Exception {
        // given — missing password field, invalid email
        String payload = """
                {"email":"not-an-email"}
                """;

        // when + then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}