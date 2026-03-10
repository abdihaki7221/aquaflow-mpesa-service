package com.aquaflow.controller;

import com.aquaflow.dto.daraja.B2BResultPayload;
import com.aquaflow.dto.response.MpesaAckResponse;
import com.aquaflow.service.B2BService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/b2b")
@RequiredArgsConstructor
@Tag(name = "B2B M-Pesa", description = "B2B payment callbacks")
@Slf4j
public class B2BCallbackController {
    private final B2BService b2bService;

    @PostMapping("/result")
    @Operation(summary = "B2B Result callback - responds immediately, processes in background")
    public Mono<MpesaAckResponse> result(@RequestBody B2BResultPayload payload) {
        log.info("[B2B-Controller] Result callback received, responding immediately to Safaricom");
        // Fire-and-forget: process in background, respond immediately
        b2bService.handleResult(payload)
                .subscribe(
                        unused -> {},
                        error -> log.error("[B2B-Controller] Error processing B2B result: {}", error.getMessage())
                );
        return Mono.just(MpesaAckResponse.accepted());
    }

    @PostMapping("/timeout")
    @Operation(summary = "B2B Timeout callback")
    public Mono<MpesaAckResponse> timeout(@RequestBody B2BResultPayload payload) {
        log.warn("[B2B-Controller] Timeout callback received");
        b2bService.handleTimeout(payload)
                .subscribe(
                        unused -> {},
                        error -> log.error("[B2B-Controller] Error processing B2B timeout: {}", error.getMessage())
                );
        return Mono.just(MpesaAckResponse.accepted());
    }
}
