package com.aquaflow.controller;

import com.aquaflow.dto.daraja.StkCallbackPayload;
import com.aquaflow.dto.request.StkPushRequestDto;
import com.aquaflow.dto.response.ApiResponse;
import com.aquaflow.dto.response.MpesaAckResponse;
import com.aquaflow.dto.response.StkPushResponseDto;
import com.aquaflow.service.StkPushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "STK Push", description = "M-Pesa STK Push (Lipa Na M-Pesa Online)")
public class StkPushController {
    private final StkPushService stkService;

    @PostMapping("/push")
    @Operation(summary = "Initiate STK Push payment prompt to customer phone")
    public Mono<ApiResponse<StkPushResponseDto>> initiate(@Valid @RequestBody StkPushRequestDto req) {
        return stkService.initiatePayment(req).map(resp -> ApiResponse.ok("STK push sent", resp));
    }

    @PostMapping("/callback")
    @Operation(summary = "STK Push callback (from Safaricom) - responds immediately, processes in background")
    public Mono<MpesaAckResponse> callback(@RequestBody StkCallbackPayload payload) {
        // Fire-and-forget: process the callback in the background
        // Respond to Safaricom immediately with ResultCode 0
        stkService.handleCallback(payload)
                .subscribe(
                        unused -> {},
                        error -> {} // Errors are logged inside handleCallback
                );
        return Mono.just(MpesaAckResponse.accepted());
    }

    @GetMapping("/status/{checkoutRequestId}")
    @Operation(summary = "Check STK push payment status")
    public Mono<ApiResponse<StkPushResponseDto>> status(@PathVariable String checkoutRequestId) {
        return stkService.getByCheckoutId(checkoutRequestId).map(ApiResponse::ok);
    }

    @GetMapping("/meter/{meterNumber}")
    @Operation(summary = "Get STK push history for a meter")
    public Mono<ApiResponse<List<StkPushResponseDto>>> byMeter(@PathVariable String meterNumber) {
        return stkService.getByMeter(meterNumber).collectList().map(ApiResponse::ok);
    }
}
