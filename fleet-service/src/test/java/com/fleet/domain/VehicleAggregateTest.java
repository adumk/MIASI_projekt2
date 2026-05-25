package com.fleet.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Vehicle aggregate — state machine and domain event emission")
class VehicleAggregateTest {

    private VehicleId vehicleId;

    @BeforeEach
    void setUp() {
        vehicleId = VehicleId.of("vehicle-001");
    }

    private Vehicle newVehicle() {
        return Vehicle.create(vehicleId, "WA12345", "Toyota", "Corolla", 2022, VehicleCategory.STANDARD);
    }

    @Nested
    @DisplayName("create() — vehicle registration")
    class Create {

        @Test
        @DisplayName("Should start as AVAILABLE and emit VehicleAdded event")
        void shouldCreateVehicleAndEmitVehicleAdded() {
            Vehicle vehicle = newVehicle();

            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
            assertThat(vehicle.getDomainEvents())
                    .hasSize(1)
                    .first()
                    .isInstanceOf(VehicleAdded.class);
        }
    }

    @Nested
    @DisplayName("rent() — vehicle handover")
    class Rent {

        @Test
        @DisplayName("Should transition to RENTED and emit VehicleStatusChanged")
        void shouldRentAvailableVehicle() {
            Vehicle vehicle = newVehicle();
            vehicle.clearDomainEvents();

            vehicle.rent();

            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.RENTED);
            assertThat(vehicle.getDomainEvents())
                    .hasSize(1)
                    .first()
                    .isInstanceOf(VehicleStatusChanged.class);
        }

        @Test
        @DisplayName("Should rent vehicle when status is RESERVED")
        void shouldRentReservedVehicle() {
            Vehicle vehicle = newVehicle();
            vehicle.clearDomainEvents();
            vehicle.reserve();

            vehicle.rent();

            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.RENTED);
        }

        @Test
        @DisplayName("Should reject rent when vehicle is not AVAILABLE or RESERVED")
        void shouldRejectRentWhenNotAvailable() {
            Vehicle vehicle = newVehicle();
            vehicle.rent();

            assertThatThrownBy(vehicle::rent)
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("reserve() — reservation hold")
    class Reserve {

        @Test
        @DisplayName("Should transition AVAILABLE to RESERVED")
        void shouldReserveVehicle() {
            Vehicle vehicle = newVehicle();
            vehicle.clearDomainEvents();

            vehicle.reserve();

            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.RESERVED);
        }
    }

    @Nested
    @DisplayName("returnVehicle() — vehicle return")
    class ReturnVehicle {

        @Test
        @DisplayName("Should transition to AVAILABLE when vehicle is RENTED")
        void shouldReturnRentedVehicle() {
            Vehicle vehicle = newVehicle();
            vehicle.rent();
            vehicle.clearDomainEvents();

            vehicle.returnVehicle();

            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
            assertThat(vehicle.getDomainEvents())
                    .filteredOn(e -> e instanceof VehicleStatusChanged)
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should reject return when vehicle is not RENTED")
        void shouldRejectReturnWhenNotRented() {
            Vehicle vehicle = newVehicle();

            assertThatThrownBy(vehicle::returnVehicle)
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("reportDamage() — damage reporting")
    class ReportDamage {

        @Test
        @DisplayName("Should record damage, set DAMAGED status and emit DamageReported")
        void shouldReportDamageForRentedVehicle() {
            Vehicle vehicle = newVehicle();
            vehicle.rent();
            vehicle.clearDomainEvents();

            vehicle.reportDamage("Scratch on bumper", DamageSeverity.MINOR);

            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.DAMAGED);
            assertThat(vehicle.getDamageRecords()).hasSize(1);
            assertThat(vehicle.getDomainEvents())
                    .filteredOn(e -> e instanceof DamageReported)
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("sendToMaintenance() — maintenance scheduling")
    class SendToMaintenance {

        @Test
        @DisplayName("Should transition to MAINTENANCE and emit MaintenanceScheduled")
        void shouldSendDamagedVehicleToMaintenance() {
            Vehicle vehicle = newVehicle();
            vehicle.reportDamage("Broken mirror", DamageSeverity.MODERATE);
            vehicle.clearDomainEvents();

            vehicle.sendToMaintenance();

            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
            assertThat(vehicle.getDomainEvents())
                    .filteredOn(e -> e instanceof MaintenanceScheduled)
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("finishMaintenance() — maintenance completion")
    class FinishMaintenance {

        @Test
        @DisplayName("Should transition to AVAILABLE when vehicle is in MAINTENANCE")
        void shouldFinishMaintenance() {
            Vehicle vehicle = newVehicle();
            vehicle.sendToMaintenance();
            vehicle.clearDomainEvents();

            vehicle.finishMaintenance();

            assertThat(vehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
            assertThat(vehicle.getDomainEvents())
                    .filteredOn(e -> e instanceof VehicleStatusChanged)
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should reject finish when vehicle is not in MAINTENANCE")
        void shouldRejectFinishWhenNotInMaintenance() {
            Vehicle vehicle = newVehicle();

            assertThatThrownBy(vehicle::finishMaintenance)
                    .isInstanceOf(InvalidStatusTransitionException.class);
        }
    }
}
