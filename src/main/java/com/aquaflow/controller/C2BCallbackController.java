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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/c2b")
@RequiredArgsConstructor
@Tag(name = "C2B Callbacks", description = "Safaricom C2B callback endpoints and URL registration")
public class C2BCallbackController {

    private final C2BService c2bService;

    /**
     * Register C2B Validation and Confirmation URLs with Safaricom.
     * Call this once to set up your callback URLs.
     */
    @PostMapping("/register-urls")
    @Operation(
            summary = "Register C2B URLs",
            description = "Registers validation and confirmation callback URLs with Safaricom Daraja API"
    )
    public Mono<ResponseEntity<ApiResponse<C2BRegisterUrlResponse>>> registerUrls() {
        return c2bService.registerUrls()
                .map(response -> ResponseEntity.ok(ApiResponse.ok(response, "C2B URLs registered successfully")))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(ApiResponse.error("C2B URL registration failed: " + e.getMessage()))));
    }

    /**
     * Validation callback URL - Safaricom sends payment details here BEFORE processing.
     * Return ResultCode 0 to accept, 1 to reject.
     *
     * NOTE: This endpoint must respond within 3 seconds or Safaricom auto-accepts.
     */
    @PostMapping("/validation")
    @Operation(
            summary = "C2B Validation Callback",
            description = "Called by Safaricom before processing a C2B payment. Validate and accept/reject."
    )
    public Mono<ResponseEntity<MpesaAckResponse>> validation(@RequestBody C2BCallbackPayload payload) {
        log.info(">>> C2B VALIDATION: transId={}, amount={}, phone={}, account={}",
                payload.getTransId(), payload.getTransAmount(),
                payload.getMsisdn(), payload.getBillRefNumber());

        return c2bService.handleValidation(payload)
                .map(tx -> ResponseEntity.ok(MpesaAckResponse.accepted()))
                .onErrorResume(e -> {
                    log.error("Validation error for transId={}: {}", payload.getTransId(), e.getMessage());
                    // Return accepted even on error to prevent customer payment failure.
                    // Log the error for investigation. Adjust as needed for your business rules.
                    return Mono.just(ResponseEntity.ok(MpesaAckResponse.accepted()));
                });
    }

    /**
     * Confirmation callback URL - Safaricom sends payment details here AFTER processing.
     * This confirms the payment was successful. Triggers B2B disbursement.
     *
     * NOTE: This endpoint must respond within 3 seconds.
     */
    @PostMapping("/confirmation")
    @Operation(
            summary = "C2B Confirmation Callback",
            description = "Called by Safaricom after a C2B payment is completed. Triggers B2B auto-disbursement."
    )
    public Mono<ResponseEntity<MpesaAckResponse>> confirmation(@RequestBody C2BCallbackPayload payload) {
        log.info(">>> C2B CONFIRMATION: transId={}, amount={}, phone={}, account={}",
                payload.getTransId(), payload.getTransAmount(),
                payload.getMsisdn(), payload.getBillRefNumber());

        // Process asynchronously but respond immediately to Safaricom
        c2bService.handleConfirmation(payload)
                .subscribe(
                        tx -> log.info("C2B confirmation processed successfully: transId={}", tx.getTransId()),
                        error -> log.error("C2B confirmation processing failed: transId={}", payload.getTransId(), error)
                );

        // Always return success to Safaricom within the timeout
        return Mono.just(ResponseEntity.ok(MpesaAckResponse.accepted()));
    }
}
