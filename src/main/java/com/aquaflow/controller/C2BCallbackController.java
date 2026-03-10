package com.aquaflow.controller;

import com.aquaflow.dto.daraja.C2BCallbackPayload;
import com.aquaflow.dto.daraja.C2BRegisterUrlResponse;
import com.aquaflow.dto.response.ApiResponse;
import com.aquaflow.dto.response.MpesaAckResponse;
import com.aquaflow.service.C2BService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/c2b")
@RequiredArgsConstructor
@Tag(name = "C2B M-Pesa", description = "C2B payment callbacks and URL registration")
@Slf4j
public class C2BCallbackController {
    private final C2BService c2bService;

    @PostMapping("/register-urls")
    @Operation(summary = "Register C2B callback URLs with Safaricom")
    public Mono<ApiResponse<C2BRegisterUrlResponse>> registerUrls() {
        return c2bService.registerUrls().map(resp -> ApiResponse.ok("URLs registered", resp));
    }

    @PostMapping("/validation")
    @Operation(summary = "C2B Validation callback (from Safaricom)")
    public Mono<MpesaAckResponse> validation(@RequestBody C2BCallbackPayload payload) {
        log.info("[C2B-Controller] Validation callback received: TransID={}", payload.getTransID());
        // Fire-and-forget validation processing, respond immediately
        c2bService.handleValidation(payload)
                .subscribe(
                        result -> log.info("[C2B-Controller] Validation processed for TransID={}", payload.getTransID()),
                        error -> log.error("[C2B-Controller] Validation processing error: {}", error.getMessage())
                );
        return Mono.just(MpesaAckResponse.accepted());
    }

    @PostMapping("/confirmation")
    @Operation(summary = "C2B Confirmation callback (from Safaricom) - responds immediately")
    public Mono<MpesaAckResponse> confirmation(@RequestBody C2BCallbackPayload payload) {
        log.info("[C2B-Controller] Confirmation callback received: TransID={}, Amount={}, Account={}",
                payload.getTransID(), payload.getTransAmount(), payload.getBillRefNumber());
        // CRITICAL: respond to Safaricom immediately, process everything in background
        c2bService.handleConfirmation(payload)
                .subscribe(
                        result -> log.info("[C2B-Controller] Confirmation fully processed for TransID={}", payload.getTransID()),
                        error -> log.error("[C2B-Controller] Confirmation processing error for TransID={}: {}",
                                payload.getTransID(), error.getMessage())
                );
        return Mono.just(MpesaAckResponse.accepted());
    }
}
