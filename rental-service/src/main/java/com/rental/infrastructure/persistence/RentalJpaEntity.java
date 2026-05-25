package com.rental.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "rentals")
public class RentalJpaEntity {

    @Id
    @Column(name = "rental_id", nullable = false)
    private String rentalId;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "payment_confirmed", nullable = false)
    private boolean paymentConfirmed;

    @Column(name = "return_mileage")
    private Integer returnMileage;

    @Column(name = "return_inspection_notes")
    private String returnInspectionNotes;

    @Column(name = "settlement_closed", nullable = false)
    private boolean settlementClosed;

    protected RentalJpaEntity() {
    }

    public RentalJpaEntity(
            String rentalId,
            String vehicleId,
            String customerId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String status,
            boolean paymentConfirmed,
            Integer returnMileage,
            String returnInspectionNotes,
            boolean settlementClosed) {
        this.rentalId = rentalId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.status = status;
        this.paymentConfirmed = paymentConfirmed;
        this.returnMileage = returnMileage;
        this.returnInspectionNotes = returnInspectionNotes;
        this.settlementClosed = settlementClosed;
    }

    public String getRentalId() {
        return rentalId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public String getStatus() {
        return status;
    }

    public boolean isPaymentConfirmed() {
        return paymentConfirmed;
    }

    public Integer getReturnMileage() {
        return returnMileage;
    }

    public String getReturnInspectionNotes() {
        return returnInspectionNotes;
    }

    public boolean isSettlementClosed() {
        return settlementClosed;
    }
}
