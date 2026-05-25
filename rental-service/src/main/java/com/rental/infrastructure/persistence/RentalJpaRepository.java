package com.rental.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RentalJpaRepository extends JpaRepository<RentalJpaEntity, String> {

    List<RentalJpaEntity> findByCustomerId(String customerId);

    List<RentalJpaEntity> findByStatus(String status);

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RentalJpaEntity r
            WHERE r.vehicleId = :vehicleId
            AND r.status IN ('RESERVED', 'ACTIVE', 'OVERDUE')
            AND r.periodStart <= :periodEnd AND r.periodEnd >= :periodStart
            AND (:excludeId IS NULL OR r.rentalId <> :excludeId)
            """)
    boolean existsOverlapping(
            @Param("vehicleId") String vehicleId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd,
            @Param("excludeId") String excludeId);

    @Query("""
            SELECT DISTINCT r.vehicleId FROM RentalJpaEntity r
            WHERE r.status IN ('RESERVED', 'ACTIVE', 'OVERDUE')
            AND r.periodStart <= :periodEnd AND r.periodEnd >= :periodStart
            """)
    List<String> findBusyVehicleIds(
            @Param("periodStart") LocalDate periodStart, @Param("periodEnd") LocalDate periodEnd);
}
