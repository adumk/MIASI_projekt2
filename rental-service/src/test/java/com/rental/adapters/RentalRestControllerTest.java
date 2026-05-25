package com.rental.adapters;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                rentalRepository)).build();
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
}
