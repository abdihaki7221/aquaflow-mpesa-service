package com.aquaflow.controller;

import com.aquaflow.dto.request.CalculateBillRequest;
import com.aquaflow.dto.request.MeterReadingRequest;
import com.aquaflow.dto.response.ApiResponse;
import com.aquaflow.dto.response.BillCalculation;
import com.aquaflow.entity.Meter;
import com.aquaflow.entity.WaterBill;
import com.aquaflow.service.MeterService;
import com.aquaflow.service.WaterBillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meters")
@RequiredArgsConstructor
@Tag(name = "Meters & Billing", description = "Water meter management and billing")
public class MeterController {
    private final MeterService meterService;
    private final WaterBillingService billingService;

    @GetMapping
    @Operation(summary = "Get all meters")
    public Mono<ApiResponse<List<Meter>>> getAll() {
        return meterService.getAllMeters().collectList().map(ApiResponse::ok);
    }

    @GetMapping("/{meterNumber}")
    @Operation(summary = "Get meter by number")
    public Mono<ApiResponse<Meter>> getByNumber(@PathVariable String meterNumber) {
        return meterService.getMeterByNumber(meterNumber).map(ApiResponse::ok);
    }

    @PostMapping("/reading")
    @Operation(summary = "Record a new meter reading")
    public Mono<ApiResponse<Meter>> recordReading(@Valid @RequestBody MeterReadingRequest req) {
        return meterService.recordReading(req).map(m -> ApiResponse.ok("Reading recorded", m));
    }

    @PostMapping("/calculate-bill")
    @Operation(summary = "Calculate bill for a meter based on current reading")
    public Mono<ApiResponse<BillCalculation>> calculateBill(@Valid @RequestBody CalculateBillRequest req) {
        return billingService.calculateBillForMeter(req.getMeterNumber(), req.getCurrentReading()).map(ApiResponse::ok);
    }

    @PostMapping("/{meterNumber}/generate-bill")
    @Operation(summary = "Generate a water bill for current period")
    public Mono<ApiResponse<WaterBill>> generateBill(@PathVariable String meterNumber) {
        return billingService.generateBill(meterNumber).map(b -> ApiResponse.ok("Bill generated", b));
    }

    @GetMapping("/{meterNumber}/bills")
    @Operation(summary = "Get all bills for a meter")
    public Mono<ApiResponse<List<WaterBill>>> getBills(@PathVariable String meterNumber) {
        return billingService.getBillsByMeter(meterNumber).collectList().map(ApiResponse::ok);
    }

    @GetMapping("/bills/unpaid")
    @Operation(summary = "Get all unpaid bills")
    public Mono<ApiResponse<List<WaterBill>>> getUnpaid() {
        return billingService.getUnpaidBills().collectList().map(ApiResponse::ok);
    }
}
