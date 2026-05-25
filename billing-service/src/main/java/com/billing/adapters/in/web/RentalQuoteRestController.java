package com.billing.adapters.in.web;

import com.billing.domain.RentalCost;
import com.billing.domain.TariffCalculator;
import com.billing.domain.VehicleCategory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/quotes")
public class RentalQuoteRestController {

    @GetMapping
    public QuoteResponse quote(
            @RequestParam VehicleCategory category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        RentalCost cost = TariffCalculator.calculate(category, startDate, endDate);
        return new QuoteResponse(
                category.name(),
                cost.getRentalDays(),
                cost.getDailyRate(),
                cost.getTotal().toMinorUnits(),
                cost.getTotal().getCurrency());
    }

    public record QuoteResponse(
            String category, int rentalDays, long dailyRateMinorUnits, long totalMinorUnits, String currency) {}
}
