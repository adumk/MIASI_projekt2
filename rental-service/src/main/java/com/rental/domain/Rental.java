package com.rental.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Rental {

    private final RentalId rentalId;
    private final VehicleId vehicleId;
    private final CustomerId customerId;
    private final DateRange period;
    private RentalStatus status;
    private Money cost;
    private boolean paymentConfirmed;
    private Integer returnMileage;
    private String returnInspectionNotes;
    private boolean settlementClosed;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Rental(
            RentalId rentalId,
            VehicleId vehicleId,
            CustomerId customerId,
            DateRange period,
            RentalStatus status) {
        this.rentalId = rentalId;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.period = period;
        this.status = status;
    }

    public static Rental create(RentalId rentalId, VehicleId vehicleId, CustomerId customerId, DateRange period) {
        return new Rental(rentalId, vehicleId, customerId, period, null);
    }

    public static Rental reconstitute(RentalId rentalId, RentalStatus status) {
        return new Rental(
                rentalId,
                VehicleId.of("unknown"),
                CustomerId.of("unknown"),
                DateRange.ofHistorical(LocalDate.now().minusDays(1), LocalDate.now()),
                status);
    }

    public static Rental reconstitute(
            RentalId rentalId,
            VehicleId vehicleId,
            CustomerId customerId,
            DateRange period,
            RentalStatus status) {
        return reconstitute(rentalId, vehicleId, customerId, period, status, false);
    }

    public static Rental reconstitute(
            RentalId rentalId,
            VehicleId vehicleId,
            CustomerId customerId,
            DateRange period,
            RentalStatus status,
            boolean paymentConfirmed) {
        return reconstitute(rentalId, vehicleId, customerId, period, status, paymentConfirmed, null, null, false);
    }

    public static Rental reconstitute(
            RentalId rentalId,
            VehicleId vehicleId,
            CustomerId customerId,
            DateRange period,
            RentalStatus status,
            boolean paymentConfirmed,
            Integer returnMileage,
            String returnInspectionNotes,
            boolean settlementClosed) {
        Rental rental = new Rental(rentalId, vehicleId, customerId, period, status);
        rental.paymentConfirmed = paymentConfirmed;
        rental.returnMileage = returnMileage;
        rental.returnInspectionNotes = returnInspectionNotes;
        rental.settlementClosed = settlementClosed;
        return rental;
    }

    public void confirm() {
        if (status != null) {
            throw new InvalidStatusTransitionException("Rental is already confirmed");
        }
        status = RentalStatus.RESERVED;
        registerEvent(new ReservationCreated(rentalId, vehicleId, customerId, period));
    }

    public void activate() {
        activate(Customer.eligible(customerId));
    }

    public void confirmPayment() {
        if (status != RentalStatus.RESERVED) {
            throw new InvalidStatusTransitionException("Payment can only be confirmed for RESERVED rentals");
        }
        if (paymentConfirmed) {
            throw new InvalidStatusTransitionException("Payment is already confirmed");
        }
        paymentConfirmed = true;
    }

    public void activate(Customer customer) {
        if (status != RentalStatus.RESERVED) {
            throw new InvalidStatusTransitionException("Rental can only be activated from RESERVED status");
        }
        if (!paymentConfirmed) {
            throw new InvalidStatusTransitionException("Payment must be confirmed before vehicle handover");
        }
        if (!customer.canRent()) {
            throw new CustomerNotEligibleException("Customer is not eligible to rent a vehicle");
        }
        status = RentalStatus.ACTIVE;
        registerEvent(new CarRented(rentalId, vehicleId, customerId, LocalDate.now()));
    }

    public void cancel() {
        if (status != RentalStatus.RESERVED) {
            throw new InvalidStatusTransitionException("Rental can only be cancelled from RESERVED status");
        }
        status = RentalStatus.CANCELLED;
        registerEvent(new RentalCancelled(rentalId, vehicleId));
    }

    public void complete(Money finalCost, LocalDate returnDate, Integer mileage, String inspectionNotes) {
        if (status != RentalStatus.ACTIVE && status != RentalStatus.OVERDUE) {
            throw new InvalidStatusTransitionException("Rental can only be completed from ACTIVE or OVERDUE status");
        }
        status = RentalStatus.COMPLETED;
        cost = finalCost;
        returnMileage = mileage;
        returnInspectionNotes = inspectionNotes;
        registerEvent(new CarReturned(
                rentalId,
                vehicleId,
                customerId,
                returnDate,
                period.getStart(),
                finalCost));
    }

    public void closeSettlement() {
        if (status != RentalStatus.COMPLETED) {
            throw new InvalidStatusTransitionException("Settlement can only be closed for COMPLETED rentals");
        }
        if (settlementClosed) {
            throw new InvalidStatusTransitionException("Settlement is already closed");
        }
        settlementClosed = true;
    }

    public void markOverdue() {
        if (status != RentalStatus.ACTIVE) {
            throw new InvalidStatusTransitionException("Only ACTIVE rentals can be marked as overdue");
        }
        status = RentalStatus.OVERDUE;
    }

    private void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public RentalId getRentalId() {
        return rentalId;
    }

    public VehicleId getVehicleId() {
        return vehicleId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public DateRange getPeriod() {
        return period;
    }

    public RentalStatus getStatus() {
        return status;
    }

    public Money getCost() {
        return cost;
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
