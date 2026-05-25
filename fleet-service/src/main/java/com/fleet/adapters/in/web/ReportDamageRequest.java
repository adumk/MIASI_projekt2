package com.fleet.adapters.in.web;

import com.fleet.domain.DamageSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportDamageRequest(@NotBlank String description, @NotNull DamageSeverity severity) {
}
