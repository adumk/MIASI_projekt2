package com.rental.adapters.in.web;

import com.rental.domain.DateRange;
import com.rental.ports.out.IRentalRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/availability")
public class AvailabilityRestController {

    private final IRentalRepository rentalRepository;

    public AvailabilityRestController(IRentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    @GetMapping("/busy-vehicles")
    public List<String> busyVehicles(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        return rentalRepository.findBusyVehicleIds(DateRange.of(startDate, endDate));
    }
}
