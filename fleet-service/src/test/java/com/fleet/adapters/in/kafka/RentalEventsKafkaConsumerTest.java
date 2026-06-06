package com.fleet.adapters.in.kafka;

import com.fleet.domain.Vehicle;
import com.fleet.domain.VehicleCategory;
import com.fleet.domain.VehicleId;
import com.fleet.domain.VehicleStatus;
import com.fleet.ports.out.IEventPublisher;
import com.fleet.ports.out.IVehicleRepository;
import com.rental.events.CarRentedEvent;
import com.rental.events.CarReturnedEvent;
import com.rental.events.RentalCancelledEvent;
import com.rental.events.ReservationCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RentalEventsKafkaConsumer — ACL handlers for rental domain events")
class RentalEventsKafkaConsumerTest {

    @Mock
    private IVehicleRepository vehicleRepository;

    @Mock
    private IEventPublisher eventPublisher;

    private CarRentedAclHandler carRentedAclHandler;
    private CarReturnedAclHandler carReturnedAclHandler;
    private ReservationCreatedAclHandler reservationCreatedAclHandler;
    private RentalCancelledAclHandler rentalCancelledAclHandler;

    private VehicleId vehicleId;

    @BeforeEach
    void setUp() {
        carRentedAclHandler = new CarRentedAclHandler(vehicleRepository, eventPublisher);
        carReturnedAclHandler = new CarReturnedAclHandler(vehicleRepository, eventPublisher);
        reservationCreatedAclHandler = new ReservationCreatedAclHandler(vehicleRepository, eventPublisher);
        rentalCancelledAclHandler = new RentalCancelledAclHandler(vehicleRepository, eventPublisher);
        vehicleId = VehicleId.of("vehicle-001");
    }

    private Vehicle availableVehicle() {
        return Vehicle.create(vehicleId, "WA12345", "Toyota", "Corolla", 2022, VehicleCategory.STANDARD);
    }

    private Vehicle rentedVehicle() {
        Vehicle vehicle = availableVehicle();
        vehicle.rent();
        vehicle.clearDomainEvents();
        return vehicle;
    }

    private Vehicle reservedVehicle() {
        Vehicle vehicle = availableVehicle();
        vehicle.reserve();
        vehicle.clearDomainEvents();
        return vehicle;
    }

    @Test
    @DisplayName("Should update vehicle status to RENTED when CarRented event received")
    void shouldUpdateVehicleStatusToRentedWhenCarRentedEventReceived() {
        // given
        Vehicle vehicle = availableVehicle();
        when(vehicleRepository.findById(vehicleId)).thenReturn(vehicle);

        CarRentedEvent event = new CarRentedEvent("rental-001", "vehicle-001", "customer-001", "2026-06-01");

        // when
        carRentedAclHandler.handle(event);

        // then
        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(VehicleStatus.RENTED);
    }

    @Test
    @DisplayName("Should return vehicle to AVAILABLE status when CarReturned event received")
    void shouldReturnVehicleToAvailableWhenCarReturnedEventReceived() {
        // given
        Vehicle vehicle = rentedVehicle();
        when(vehicleRepository.findById(vehicleId)).thenReturn(vehicle);

        CarReturnedEvent event = new CarReturnedEvent("rental-001", "vehicle-001", "customer-001", "2026-06-08");

        // when
        carReturnedAclHandler.handle(event);

        // then
        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    @DisplayName("Should reserve vehicle when ReservationCreated event received")
    void shouldReserveVehicleWhenReservationCreatedEventReceived() {
        // given
        Vehicle vehicle = availableVehicle();
        when(vehicleRepository.findById(vehicleId)).thenReturn(vehicle);

        ReservationCreatedEvent event = new ReservationCreatedEvent(
                "rental-001", "vehicle-001", "customer-001", "2026-06-01", "2026-06-08");

        // when
        reservationCreatedAclHandler.handle(event);

        // then
        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(VehicleStatus.RESERVED);
    }

    @Test
    @DisplayName("Should release reservation to AVAILABLE when RentalCancelled event received")
    void shouldReleaseReservationWhenRentalCancelledEventReceived() {
        // given
        Vehicle vehicle = reservedVehicle();
        when(vehicleRepository.findById(vehicleId)).thenReturn(vehicle);

        RentalCancelledEvent event = new RentalCancelledEvent("rental-001", "vehicle-001");

        // when
        rentalCancelledAclHandler.handle(event);

        // then
        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }
}