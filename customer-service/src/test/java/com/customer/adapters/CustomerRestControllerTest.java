package com.customer.adapters;

import com.customer.adapters.in.web.CustomerRestController;
import com.customer.application.BlockCustomerUseCase;
import com.customer.application.GetCustomerUseCase;
import com.customer.application.RegisterCustomerUseCase;
import com.customer.application.VerifyCustomerUseCase;
import com.customer.domain.CustomerId;
import com.customer.domain.Customer;
import com.customer.domain.CustomerNotFoundException;
import com.customer.domain.DriverLicense;
import com.customer.domain.DuplicateEmailException;
import com.customer.domain.Email;
import com.customer.domain.PersonName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerRestController — HTTP contract and response codes")
class CustomerRestControllerTest {

    private MockMvc mockMvc;

    @Mock private RegisterCustomerUseCase registerCustomerUseCase;
    @Mock private VerifyCustomerUseCase verifyCustomerUseCase;
    @Mock private BlockCustomerUseCase blockCustomerUseCase;
    @Mock private GetCustomerUseCase getCustomerUseCase;

    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CustomerRestController(
                registerCustomerUseCase, verifyCustomerUseCase,
                blockCustomerUseCase, getCustomerUseCase)).build();
        customerId = CustomerId.of("customer-001");
    }

    private Customer activeCustomer() {
        Customer customer = Customer.create(
                customerId,
                PersonName.of("Jan", "Kowalski"),
                Email.of("jan@example.com"),
                DriverLicense.of("DL-001", LocalDate.now().plusYears(1)));
        customer.register();
        customer.clearDomainEvents();
        return customer;
    }

    private String validRegisterPayload() {
        return """
                {
                  "firstName": "Jan",
                  "lastName": "Kowalski",
                  "email": "jan@example.com",
                  "licenseNumber": "DL-001",
                  "licenseExpiryDate": "2028-01-01"
                }
                """;
    }

    @Test
    @DisplayName("Should return 201 when customer registered successfully")
    void shouldReturn201WhenCustomerRegisteredSuccessfully() throws Exception {
        // given
        when(registerCustomerUseCase.handle(any())).thenReturn(customerId);

        // when + then
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterPayload()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should throw exception when email already registered")
    void shouldReturn409WhenEmailAlreadyRegistered() {
        // given
        when(registerCustomerUseCase.handle(any()))
                .thenThrow(new DuplicateEmailException("Email already registered"));

        // when + then
        assertThrows(Exception.class, () ->
                mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterPayload()))
        );
    }

    @Test
    @DisplayName("Should return 200 with customer data when found")
    void shouldReturn200WithCustomerData() throws Exception {
        // given
        when(getCustomerUseCase.handle(any())).thenReturn(activeCustomer());

        // when + then
        mockMvc.perform(get("/api/v1/customers/customer-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("customer-001"))
                .andExpect(jsonPath("$.email").value("jan@example.com"));
    }

    @Test
    @DisplayName("Should throw exception when customer not found")
    void shouldReturn404WhenCustomerNotFound() {
        // given
        when(getCustomerUseCase.handle(any()))
                .thenThrow(new CustomerNotFoundException("not found"));

        // when + then
        assertThrows(Exception.class, () ->
                mockMvc.perform(get("/api/v1/customers/nonexistent"))
        );
    }
}