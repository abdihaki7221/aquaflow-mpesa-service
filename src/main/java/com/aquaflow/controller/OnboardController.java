package com.aquaflow.controller;

import com.aquaflow.dto.request.OnboardRequest;
import com.aquaflow.dto.response.ApiResponse;
import com.aquaflow.entity.Meter;
import com.aquaflow.service.MeterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/onboard")
@RequiredArgsConstructor
@Tag(name = "Onboarding", description = "Customer self-onboarding")
public class OnboardController {
    private final MeterService meterService;

    @PostMapping
    @Operation(summary = "Submit self-onboarding application")
    public Mono<ApiResponse<String>> onboard(@Valid @RequestBody OnboardRequest req) {
        Meter meter = Meter.builder()
                .meterNumber("M-" + String.format("%04d", (int)(Math.random() * 9999)))
                .tenantName(req.getFirstName() + " " + req.getLastName())
                .unitNumber(req.getUnitNumber())
                .phone(req.getPhone()).email(req.getEmail())
                .address(req.getPropertyName() + ", Unit " + req.getUnitNumber())
                .previousReading(0L).currentReading(0L)
                .status("PENDING").build();
        return meterService.createMeter(meter)
                .map(m -> ApiResponse.ok("Application submitted. We will email your credentials within 24-48 hours.",
                        "Reference: " + m.getMeterNumber()));
    }
}
