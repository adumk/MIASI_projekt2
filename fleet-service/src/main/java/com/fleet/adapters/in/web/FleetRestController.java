package com.fleet.adapters.in.web;

import com.fleet.application.AddVehicleCommand;
import com.fleet.application.AddVehicleUseCase;
import com.fleet.application.CompleteMaintenanceCommand;
import com.fleet.application.CompleteMaintenanceUseCase;
import com.fleet.application.RemoveVehicleCommand;
import com.fleet.application.RemoveVehicleUseCase;
import com.fleet.application.ReportDamageCommand;
import com.fleet.application.ReportDamageUseCase;
import com.fleet.application.ScheduleMaintenanceCommand;
import com.fleet.application.ScheduleMaintenanceUseCase;
import com.fleet.application.SearchVehiclesQuery;
import com.fleet.application.SearchVehiclesUseCase;
import com.fleet.application.UpdateVehicleStatusCommand;
import com.fleet.application.UpdateVehicleStatusUseCase;
import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleId;
import com.fleet.domain.VehicleNotFoundException;
import com.fleet.domain.VehicleStatus;
import com.fleet.infrastructure.VehiclePricingService;
import com.fleet.ports.out.IVehicleRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
public class FleetRestController {

    private final AddVehicleUseCase addVehicleUseCase;
    private final ReportDamageUseCase reportDamageUseCase;
    private final UpdateVehicleStatusUseCase updateVehicleStatusUseCase;
    private final ScheduleMaintenanceUseCase scheduleMaintenanceUseCase;
    private final CompleteMaintenanceUseCase completeMaintenanceUseCase;
    private final RemoveVehicleUseCase removeVehicleUseCase;
    private final SearchVehiclesUseCase searchVehiclesUseCase;
    private final IVehicleRepository vehicleRepository;
    private final VehiclePricingService pricingService;

    public FleetRestController(
            AddVehicleUseCase addVehicleUseCase,
            ReportDamageUseCase reportDamageUseCase,
            UpdateVehicleStatusUseCase updateVehicleStatusUseCase,
            ScheduleMaintenanceUseCase scheduleMaintenanceUseCase,
            CompleteMaintenanceUseCase completeMaintenanceUseCase,
            RemoveVehicleUseCase removeVehicleUseCase,
            SearchVehiclesUseCase searchVehiclesUseCase,
            IVehicleRepository vehicleRepository,
            VehiclePricingService pricingService) {
        this.addVehicleUseCase = addVehicleUseCase;
        this.reportDamageUseCase = reportDamageUseCase;
        this.updateVehicleStatusUseCase = updateVehicleStatusUseCase;
        this.scheduleMaintenanceUseCase = scheduleMaintenanceUseCase;
        this.completeMaintenanceUseCase = completeMaintenanceUseCase;
        this.removeVehicleUseCase = removeVehicleUseCase;
        this.searchVehiclesUseCase = searchVehiclesUseCase;
        this.vehicleRepository = vehicleRepository;
        this.pricingService = pricingService;
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return VehicleResponse.fromDomain(vehicle, pricingService.dailyRateMinorUnits(vehicle.getCategory()));
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> addVehicle(@Valid @RequestBody AddVehicleRequest request) {
        VehicleId vehicleId = request.vehicleId() != null && !request.vehicleId().isBlank()
                ? VehicleId.of(request.vehicleId())
                : VehicleId.generate();
        Vehicle vehicle = addVehicleUseCase.handle(new AddVehicleCommand(
                vehicleId,
                request.licensePlate(),
                request.brand(),
                request.model(),
                request.year(),
                request.category()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(vehicle));
    }

    @GetMapping
    public List<VehicleResponse> searchVehicles(
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) VehicleCategory category,
            @RequestParam(required = false) Long maxDailyRateMinorUnits) {
        return searchVehiclesUseCase.handle(new SearchVehiclesQuery(status, category)).stream()
                .map(this::toResponse)
                .filter(v -> maxDailyRateMinorUnits == null || v.dailyRateMinorUnits() <= maxDailyRateMinorUnits)
                .toList();
    }

    @GetMapping("/{id}")
    public VehicleResponse getVehicle(@PathVariable("id") String id) {
        Vehicle vehicle = vehicleRepository.findById(VehicleId.of(id));
        if (vehicle == null) {
            throw new VehicleNotFoundException("Vehicle not found: " + id);
        }
        return toResponse(vehicle);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable("id") String id, @Valid @RequestBody UpdateVehicleStatusRequest request) {
        updateVehicleStatusUseCase.handle(
                new UpdateVehicleStatusCommand(VehicleId.of(id), request.status()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/damage")
    public ResponseEntity<Void> reportDamage(
            @PathVariable("id") String id, @Valid @RequestBody ReportDamageRequest request) {
        reportDamageUseCase.handle(new ReportDamageCommand(
                VehicleId.of(id), request.description(), request.severity()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/maintenance")
    public ResponseEntity<Void> scheduleMaintenance(@PathVariable("id") String id) {
        scheduleMaintenanceUseCase.handle(new ScheduleMaintenanceCommand(VehicleId.of(id)));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/maintenance/complete")
    public ResponseEntity<Void> completeMaintenance(@PathVariable("id") String id) {
        completeMaintenanceUseCase.handle(new CompleteMaintenanceCommand(VehicleId.of(id)));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeVehicle(@PathVariable("id") String id) {
        removeVehicleUseCase.handle(new RemoveVehicleCommand(VehicleId.of(id)));
        return ResponseEntity.noContent().build();
    }
}
