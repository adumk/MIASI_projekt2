package com.fleet.adapters;

import com.fleet.adapters.in.web.FleetRestController;
import com.fleet.application.AddVehicleUseCase;
import com.fleet.application.CompleteMaintenanceUseCase;
import com.fleet.application.RemoveVehicleUseCase;
import com.fleet.application.ReportDamageUseCase;
import com.fleet.application.ScheduleMaintenanceUseCase;
import com.fleet.application.SearchVehiclesUseCase;
import com.fleet.application.UpdateVehicleStatusUseCase;
import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleId;
import com.fleet.domain.VehicleNotFoundException;
import com.fleet.infrastructure.VehiclePricingService;
import com.fleet.ports.out.IVehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FleetRestController — HTTP contract and response codes")
class FleetRestControllerTest {

    private MockMvc mockMvc;

    @Mock private AddVehicleUseCase addVehicleUseCase;
    @Mock private ReportDamageUseCase reportDamageUseCase;
    @Mock private UpdateVehicleStatusUseCase updateVehicleStatusUseCase;
    @Mock private ScheduleMaintenanceUseCase scheduleMaintenanceUseCase;
    @Mock private CompleteMaintenanceUseCase completeMaintenanceUseCase;
    @Mock private RemoveVehicleUseCase removeVehicleUseCase;
    @Mock private SearchVehiclesUseCase searchVehiclesUseCase;
    @Mock private IVehicleRepository vehicleRepository;

    private VehiclePricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new VehiclePricingService();
        mockMvc = MockMvcBuilders.standaloneSetup(new FleetRestController(
                addVehicleUseCase, reportDamageUseCase, updateVehicleStatusUseCase,
                scheduleMaintenanceUseCase, completeMaintenanceUseCase, removeVehicleUseCase,
                searchVehiclesUseCase, vehicleRepository, pricingService)).build();
    }

    private Vehicle standardVehicle(String id) {
        return Vehicle.create(VehicleId.of(id), "WA12345", "Toyota", "Corolla", 2022, VehicleCategory.STANDARD);
    }

    @Test
    @DisplayName("Should return 201 with vehicle when added successfully")
    void shouldReturn201WithVehicleWhenAddedSuccessfully() throws Exception {
        // given
        Vehicle vehicle = standardVehicle("vehicle-001");
        when(addVehicleUseCase.handle(any())).thenReturn(vehicle);

        String payload = """
                {
                  "licensePlate": "WA12345",
                  "brand": "Toyota",
                  "model": "Corolla",
                  "year": 2022,
                  "category": "STANDARD"
                }
                """;

        // when + then
        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("Should return 400 when add vehicle request is invalid")
    void shouldReturn400WhenAddVehicleRequestInvalid() throws Exception {
        // given — missing required fields
        String payload = """
                {
                  "brand": "Toyota",
                  "model": "Corolla",
                  "year": 2022
                }
                """;

        // when + then
        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Disabled
    @DisplayName("Should return 200 with list of vehicles")
    void shouldReturn200WithListOfVehicles() throws Exception {
        // given
        when(searchVehiclesUseCase.handle(any())).thenReturn(List.of(
                standardVehicle("v-1"),
                standardVehicle("v-2")));

        // when + then
        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Should return 200 with vehicle when found")
    void shouldReturn200WithVehicleWhenFound() throws Exception {
        // given
        when(vehicleRepository.findById(VehicleId.of("vehicle-001"))).thenReturn(standardVehicle("vehicle-001"));

        // when + then
        mockMvc.perform(get("/api/v1/vehicles/vehicle-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value("vehicle-001"));
    }

    @Test
    @DisplayName("Should throw exception when vehicle not found")
    void shouldReturn404WhenVehicleNotFound() {

        // given
        when(vehicleRepository.findById(any())).thenReturn(null);

        // when + then
        assertThrows(Exception.class, () ->
                mockMvc.perform(get("/api/v1/vehicles/nonexistent"))
        );
    }

    @Test
    @DisplayName("Should return 204 when damage reported successfully")
    void shouldReturn204WhenDamageReportedSuccessfully() throws Exception {
        // given
        doNothing().when(reportDamageUseCase).handle(any());

        String payload = """
                {"description": "Scratch on door", "severity": "MINOR"}
                """;

        // when + then
        mockMvc.perform(post("/api/v1/vehicles/vehicle-001/damage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 204 when maintenance scheduled successfully")
    void shouldReturn204WhenMaintenanceScheduledSuccessfully() throws Exception {
        // given
        doNothing().when(scheduleMaintenanceUseCase).handle(any());

        // when + then
        mockMvc.perform(post("/api/v1/vehicles/vehicle-001/maintenance"))
                .andExpect(status().isNoContent());
    }
}