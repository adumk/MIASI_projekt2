package com.rental.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Vehicle — aggregate behaviour")
class VehicleTest {

    private static final VehicleId VEHICLE_ID = VehicleId.of(UUID.randomUUID());

    // -------------------------------------------------------------------------
    // Creation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should create vehicle in AVAILABLE status")
    void shouldCreateVehicleInAvailableStatus() {
        Vehicle vehicle = Vehicle.create(VEHICLE_ID);

        assertThat(vehicle).isNotNull();
        assertThat(vehicle.id()).isEqualTo(VEHICLE_ID);
        assertThat(vehicle.status()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    // -------------------------------------------------------------------------
    // Renting
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should rent vehicle when it is AVAILABLE")
    void shouldRentVehicleOnlyWhenAvailable() {
        Vehicle vehicle = Vehicle.create(VEHICLE_ID);
        vehicle.rent();

        assertThat(vehicle.status()).isEqualTo(VehicleStatus.RENTED);
    }

    @Test
    @DisplayName("should reject rent when vehicle is not AVAILABLE")
    void shouldRejectRentWhenVehicleIsNotAvailable() {
        Vehicle vehicle = Vehicle.create(VEHICLE_ID);
        vehicle.rent();

        assertThatThrownBy(vehicle::rent)
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    // -------------------------------------------------------------------------
    // Returning
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should return vehicle to AVAILABLE status after being RENTED")
    void shouldReturnVehicleToAvailableStatus() {
        Vehicle vehicle = Vehicle.create(VEHICLE_ID);
        vehicle.rent();
        vehicle.returnVehicle();

        assertThat(vehicle.status()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    // -------------------------------------------------------------------------
    // Damage
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should mark vehicle as DAMAGED after damage report")
    void shouldReportDamageAndMarkVehicleAsDamaged() {
        Vehicle vehicle = Vehicle.create(VEHICLE_ID);
        vehicle.reportDamage();

        assertThat(vehicle.status()).isEqualTo(VehicleStatus.DAMAGED);
    }

    // -------------------------------------------------------------------------
    // Maintenance
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should send vehicle to MAINTENANCE")
    void shouldSendVehicleToMaintenance() {
        Vehicle vehicle = Vehicle.create(VEHICLE_ID);
        vehicle.sendToMaintenance();

        assertThat(vehicle.status()).isEqualTo(VehicleStatus.MAINTENANCE);
    }

    @Test
    @DisplayName("should mark vehicle as AVAILABLE after finishing maintenance")
    void shouldFinishMaintenanceAndMarkVehicleAsAvailable() {
        Vehicle vehicle = Vehicle.create(VEHICLE_ID);
        vehicle.sendToMaintenance();
        vehicle.finishMaintenance();

        assertThat(vehicle.status()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    // -------------------------------------------------------------------------
    // Domain events
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should emit VehicleStatusChanged event on each valid status transition")
    void shouldEmitVehicleStatusChangedEvent() {
        Vehicle vehicle = Vehicle.create(VEHICLE_ID);
        vehicle.clearDomainEvents();
        vehicle.rent();

        assertThat(vehicle.domainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(VehicleStatusChanged.class);

        VehicleStatusChanged event = (VehicleStatusChanged) vehicle.domainEvents().get(0);
        assertThat(event.vehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(event.newStatus()).isEqualTo(VehicleStatus.RENTED);
    }

    @Test
    @DisplayName("should emit DamageReported event when damage is reported")
    void shouldEmitDamageReportedEvent() {
        Vehicle vehicle = Vehicle.create(VEHICLE_ID);
        vehicle.clearDomainEvents();
        vehicle.reportDamage();

        assertThat(vehicle.domainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(DamageReported.class);

        DamageReported event = (DamageReported) vehicle.domainEvents().get(0);
        assertThat(event.vehicleId()).isEqualTo(VEHICLE_ID);
    }

    // -------------------------------------------------------------------------
    // Invalid transitions
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should reject invalid status transition — return when not RENTED")
    void shouldRejectInvalidStatusTransition() {
        Vehicle vehicle = Vehicle.create(VEHICLE_ID);

        assertThatThrownBy(vehicle::returnVehicle)
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    // -------------------------------------------------------------------------
    // Obvious invariants
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should reject null vehicle id on creation")
    void shouldRejectNullVehicleId() {
        assertThatThrownBy(() -> Vehicle.create(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}