package com.rental.adapters.in.web;

import com.rental.domain.RentalStatus;
import com.rental.ports.out.IRentalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/reports")
public class AdminReportController {

    private final IRentalRepository rentalRepository;

    public AdminReportController(IRentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    @GetMapping("/rentals")
    public ResponseEntity<Map<String, Long>> rentalStatusReport() {
        Map<String, Long> report = new LinkedHashMap<>();
        for (RentalStatus status : RentalStatus.values()) {
            report.put(status.name(), rentalRepository.countByStatus(status));
        }
        return ResponseEntity.ok(report);
    }
}
