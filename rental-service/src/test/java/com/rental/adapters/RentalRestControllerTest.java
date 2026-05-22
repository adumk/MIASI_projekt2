package com.rental.adapters;

import com.rental.adapters.in.web.RentalRestController;
import com.rental.application.CreateReservationUseCase;
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

@ExtendWith(MockitoExtension.class) // Czyste Mockito, brak kontekstu Springa
@DisplayName("RentalRestController — web contract and bean validation")
class RentalRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateReservationUseCase createReservationUseCase;

    private static final String INVALID_RESERVATION_JSON = """
            {
              "customerId": "",
              "vehicleId": "vehicle-001",
              "email": "not-an-email",
              "startDate": "2026-06-01",
              "endDate": "2026-06-07"
            }
            """;

    @BeforeEach
    void setUp() {
        // Konfiguracja izolowanego, błyskawicznego środowiska testowego web-layer
        mockMvc = MockMvcBuilders.standaloneSetup(new RentalRestController(createReservationUseCase)).build();
    }

    @Test
    @DisplayName("Should return HTTP 400 when reservation payload violates bean validation")
    void shouldRejectInvalidReservationPayload() throws Exception {
        // given
        String invalidReservationJson = INVALID_RESERVATION_JSON;

        // when + then
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidReservationJson))
                .andExpect(status().isBadRequest());
    }
}