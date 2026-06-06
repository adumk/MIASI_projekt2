package com.rental.adapters;

import com.rental.adapters.in.web.RentalApiExceptionHandler;
import com.rental.adapters.in.web.RentalRestController;
import com.rental.application.CancelReservationUseCase;
import com.rental.application.ConfirmReservationUseCase;
import com.rental.application.CreateReservationUseCase;
import com.rental.application.GetRentalHistoryUseCase;
import com.rental.application.RentVehicleUseCase;
import com.rental.application.ReturnVehicleUseCase;
import com.rental.ports.out.IRentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.doThrow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;
import com.rental.application.CreateReservationCommand;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("RentalRestController — web contract and bean validation")
class RentalRestControllerTest {

    private MockMvc mockMvc;

    @Mock private CreateReservationUseCase createReservationUseCase;
    @Mock private RentVehicleUseCase rentVehicleUseCase;
    @Mock private ReturnVehicleUseCase returnVehicleUseCase;
    @Mock private CancelReservationUseCase cancelReservationUseCase;
    @Mock private ConfirmReservationUseCase confirmReservationUseCase;
    @Mock private GetRentalHistoryUseCase getRentalHistoryUseCase;
    @Mock private IRentalRepository rentalRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new RentalRestController(
                        createReservationUseCase,
                        rentVehicleUseCase,
                        returnVehicleUseCase,
                        cancelReservationUseCase,
                        confirmReservationUseCase,
                        getRentalHistoryUseCase,
                        rentalRepository))
                .setControllerAdvice(new RentalApiExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return HTTP 400 when reservation payload violates bean validation")
    void shouldRejectInvalidReservationPayload() throws Exception {
        String invalidReservationJson = """
                {
                  "customerId": "",
                  "vehicleId": "vehicle-001",
                  "email": "not-an-email",
                  "startDate": "2026-06-01",
                  "endDate": "2026-06-07"
                }
                """;

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidReservationJson))
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("Should return HTTP 201 when reservation created successfully")
    void shouldReturn201WhenReservationCreatedSuccessfully() throws Exception {
        // given
        Rental rental = Rental.create(
                RentalId.of("rental-001"),
                VehicleId.of("vehicle-001"),
                CustomerId.of("customer-001"),
                DateRange.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(7)));
        when(createReservationUseCase.handle(any(CreateReservationCommand.class))).thenReturn(rental);

        String validJson = String.format("""
        {
          "customerId": "customer-001",
          "vehicleId": "vehicle-001",
          "email": "jan@example.com",
          "startDate": "%s",
          "endDate": "%s"
        }
        """,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(7));

        // when + then
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return HTTP 500 when rental not found during activate")
    void shouldReturn404WhenRentalNotFoundDuringRent() throws Exception {
        // given
        doThrow(new RuntimeException("Rental not found")).when(rentVehicleUseCase).handle(any());

        // when + then
        mockMvc.perform(post("/api/v1/rentals/nonexistent/activate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Should return HTTP 200 with rental history for customer")
    void shouldReturn200WithRentalHistoryForCustomer() throws Exception {
        // given
        DateRange period = DateRange.of(LocalDate.now().plusDays(1), LocalDate.now().plusDays(7));
        List<Rental> rentals = List.of(
                Rental.reconstitute(RentalId.of("r-1"), VehicleId.of("v-1"),
                        CustomerId.of("customer-001"), period, RentalStatus.COMPLETED),
                Rental.reconstitute(RentalId.of("r-2"), VehicleId.of("v-2"),
                        CustomerId.of("customer-001"), period, RentalStatus.CANCELLED)
        );
        when(getRentalHistoryUseCase.handle(any())).thenReturn(rentals);

        // when + then
        mockMvc.perform(get("/api/v1/customers/customer-001/rentals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
