package com.rental.adapters;

import com.rental.adapters.in.web.RentalRestController;
import com.rental.application.CreateReservationUseCase;
import com.rental.domain.VehicleNotAvailableException;
import com.rental.domain.VehicleId;
import com.rental.domain.RentalNotFoundException;
import com.rental.domain.RentalId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("RentalRestController — web contract and bean validation")
class RentalRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CreateReservationUseCase createReservationUseCase;

    private static final String VALID_RESERVATION_JSON = """
            {
              "customerId": "%s",
              "vehicleId": "%s",
              "startDate": "2026-06-01",
              "endDate": "2026-06-07"
            }
            """.formatted(UUID.randomUUID(), UUID.randomUUID());

    private static final String INVALID_RESERVATION_JSON = """
            {
              "customerId": "",
              "vehicleId": "vehicle-001",
              "email": "not-an-email",
              "startDate": "2026-06-01",
              "endDate": "2026-06-07"
            }
            """;

    private static final String MALFORMED_JSON = """
            {
              "customerId": "%s",
              "vehicleId":
            """.formatted(UUID.randomUUID());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RentalRestController(createReservationUseCase))
                .build();
    }

    @Test
    @DisplayName("should return HTTP 400 when reservation payload violates bean validation")
    void shouldRejectInvalidReservationPayload() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_RESERVATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return HTTP 201 when reservation request is valid")
    void shouldCreateReservationWhenRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_RESERVATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("should return HTTP 400 when reservation payload violates bean validation — explicit alias")
    void shouldReturnBadRequestWhenReservationPayloadViolatesBeanValidation() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALID_RESERVATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return HTTP 409 when vehicle is unavailable")
    void shouldReturnConflictWhenVehicleIsUnavailable() throws Exception {
        doThrow(new VehicleNotAvailableException(VehicleId.of(UUID.randomUUID())))
                .when(createReservationUseCase).handle(any());

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_RESERVATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("should return HTTP 404 when referenced aggregate does not exist")
    void shouldReturnNotFoundWhenReferencedAggregateDoesNotExist() throws Exception {
        doThrow(new RentalNotFoundException(RentalId.of(UUID.randomUUID())))
                .when(createReservationUseCase).handle(any());

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_RESERVATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should map use case result to HTTP response body containing rentalId")
    void shouldMapCreateReservationResponseToHttpResponseBody() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_RESERVATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rentalId").exists());
    }

    @Test
    @DisplayName("should return HTTP 400 when JSON is malformed")
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MALFORMED_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should bind request DTO to command and delegate to use case exactly once")
    void shouldBindRequestDTOToCommandExactlyOnce() throws Exception {
        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_RESERVATION_JSON))
                .andExpect(status().isCreated());

        verify(createReservationUseCase, times(1)).handle(any());
    }
}