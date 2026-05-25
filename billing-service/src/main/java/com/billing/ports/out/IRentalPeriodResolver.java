package com.billing.ports.out;

import java.time.LocalDate;

public interface IRentalPeriodResolver {

    LocalDate resolveStartDate(String rentalId, LocalDate returnDate);
}
