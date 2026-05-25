package com.rental.ports.out;

import com.rental.domain.Money;

import java.time.LocalDate;

public interface IBillingQuotePort {

    Money quoteRentalCost(String vehicleCategory, LocalDate startDate, LocalDate endDate);
}
