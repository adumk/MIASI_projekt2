package com.billing.adapters.in.web;

import com.billing.domain.TariffRegistry;
import com.billing.domain.VehicleCategory;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tariffs")
public class TariffRestController {

    @GetMapping
    public Map<String, Long> getTariffs() {
        return TariffRegistry.snapshot();
    }

    @PutMapping("/{category}")
    public ResponseEntity<Map<String, Long>> updateTariff(
            @PathVariable("category") VehicleCategory category,
            @RequestBody UpdateTariffRequest request) {
        TariffRegistry.setDailyRate(category, request.dailyRateMinorUnits());
        return ResponseEntity.ok(TariffRegistry.snapshot());
    }

    public record UpdateTariffRequest(@Min(1) long dailyRateMinorUnits) {}
}
