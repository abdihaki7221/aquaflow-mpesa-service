package com.aquaflow.controller;

import com.aquaflow.dto.daraja.B2BResultPayload;
import com.aquaflow.dto.response.MpesaAckResponse;
import com.aquaflow.service.B2BService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/b2b")
@RequiredArgsConstructor
@Tag(name = "B2B M-Pesa", description = "B2B payment callbacks")
public class B2BCallbackController {
    private final B2BService b2bService;

    @PostMapping("/result")
    @Operation(summary = "B2B Result callback")
    public Mono<MpesaAckResponse> result(@RequestBody B2BResultPayload payload) {
        return b2bService.handleResult(payload).thenReturn(MpesaAckResponse.accepted());
    }

    @PostMapping("/timeout")
    @Operation(summary = "B2B Timeout callback")
    public Mono<MpesaAckResponse> timeout(@RequestBody B2BResultPayload payload) {
        return b2bService.handleTimeout(payload).thenReturn(MpesaAckResponse.accepted());
    }
}
