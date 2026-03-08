package com.aquaflow.controller;

import com.aquaflow.dto.daraja.C2BCallbackPayload;
import com.aquaflow.dto.daraja.C2BRegisterUrlResponse;
import com.aquaflow.dto.response.ApiResponse;
import com.aquaflow.dto.response.MpesaAckResponse;
import com.aquaflow.service.C2BService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/c2b")
@RequiredArgsConstructor
@Tag(name = "C2B M-Pesa", description = "C2B payment callbacks and URL registration")
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
        return c2bService.handleValidation(payload);
    }

    @PostMapping("/confirmation")
    @Operation(summary = "C2B Confirmation callback (from Safaricom)")
    public Mono<MpesaAckResponse> confirmation(@RequestBody C2BCallbackPayload payload) {
        return c2bService.handleConfirmation(payload);
    }
}
