package com.rental.adapters.out.db;

import com.rental.domain.CustomerId;
import com.rental.domain.DateRange;
import com.rental.domain.Rental;
import com.rental.domain.RentalId;
import com.rental.domain.RentalStatus;
import com.rental.domain.VehicleId;
import com.rental.infrastructure.persistence.RentalJpaEntity;
import com.rental.infrastructure.persistence.RentalJpaRepository;
import com.rental.ports.out.IRentalRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostgresRentalRepositoryAdapter implements IRentalRepository {

    private final RentalJpaRepository jpaRepository;

    public PostgresRentalRepositoryAdapter(RentalJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Rental rental) {
        RentalJpaEntity entity = new RentalJpaEntity(
                rental.getRentalId().getValue(),
                rental.getVehicleId().getValue(),
                rental.getCustomerId().getValue(),
                rental.getPeriod().getStart(),
                rental.getPeriod().getEnd(),
                rental.getStatus().name(),
                rental.isPaymentConfirmed(),
                rental.getReturnMileage(),
                rental.getReturnInspectionNotes(),
                rental.isSettlementClosed());
        jpaRepository.save(entity);
    }

    @Override
    public Rental findById(RentalId rentalId) {
        return jpaRepository.findById(rentalId.getValue())
                .map(this::toDomain)
                .orElse(null);
    }

    @Override
    public List<Rental> findByCustomerId(CustomerId customerId) {
        return jpaRepository.findByCustomerId(customerId.getValue()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Rental> findActiveRentals() {
        return jpaRepository.findByStatus(RentalStatus.ACTIVE.name()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Rental> findByStatus(RentalStatus status) {
        return jpaRepository.findByStatus(status.name()).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Rental> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public long countByStatus(RentalStatus status) {
        return jpaRepository.findByStatus(status.name()).size();
    }

    @Override
    public boolean hasOverlappingBooking(VehicleId vehicleId, DateRange period, RentalId excludeRentalId) {
        String exclude = excludeRentalId != null ? excludeRentalId.getValue() : null;
        return jpaRepository.existsOverlapping(
                vehicleId.getValue(), period.getStart(), period.getEnd(), exclude);
    }

    @Override
    public List<String> findBusyVehicleIds(DateRange period) {
        return jpaRepository.findBusyVehicleIds(period.getStart(), period.getEnd());
    }

    private Rental toDomain(RentalJpaEntity entity) {
        return Rental.reconstitute(
                RentalId.of(entity.getRentalId()),
                VehicleId.of(entity.getVehicleId()),
                CustomerId.of(entity.getCustomerId()),
                DateRange.ofHistorical(entity.getPeriodStart(), entity.getPeriodEnd()),
                RentalStatus.valueOf(entity.getStatus()),
                entity.isPaymentConfirmed(),
                entity.getReturnMileage(),
                entity.getReturnInspectionNotes(),
                entity.isSettlementClosed());
    }
}
