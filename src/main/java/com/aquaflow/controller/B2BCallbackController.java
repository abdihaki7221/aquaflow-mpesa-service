package com.aquaflow.controller;

import com.aquaflow.dto.daraja.B2BResultPayload;
import com.aquaflow.dto.response.MpesaAckResponse;
import com.aquaflow.service.B2BService;
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
@RequestMapping("/api/v1/mpesa/b2b")
@RequiredArgsConstructor
@Tag(name = "B2B Callbacks", description = "Safaricom B2B payment result and timeout callbacks")
public class B2BCallbackController {

    private final B2BService b2bService;

    /**
     * B2B Result callback - Safaricom sends the B2B payment result here.
     */
    @PostMapping("/result")
    @Operation(
            summary = "B2B Result Callback",
            description = "Called by Safaricom with B2B payment results (success or failure)"
    )
    public Mono<ResponseEntity<MpesaAckResponse>> result(@RequestBody B2BResultPayload payload) {
        log.info(">>> B2B RESULT: conversationId={}, resultCode={}",
                payload.getResult().getConversationId(),
                payload.getResult().getResultCode());

        // Process asynchronously, respond immediately
        b2bService.handleResult(payload)
                .subscribe(
                        b2b -> log.info("B2B result processed: id={}, status={}", b2b.getId(), b2b.getStatus()),
                        error -> log.error("B2B result processing failed", error)
                );

        return Mono.just(ResponseEntity.ok(MpesaAckResponse.accepted()));
    }

    /**
     * B2B Timeout callback - Safaricom sends timeout notifications here.
     */
    @PostMapping("/timeout")
    @Operation(
            summary = "B2B Timeout Callback",
            description = "Called by Safaricom when B2B payment request times out"
    )
    public Mono<ResponseEntity<MpesaAckResponse>> timeout(@RequestBody B2BResultPayload payload) {
        log.warn(">>> B2B TIMEOUT: conversationId={}",
                payload.getResult().getConversationId());

        b2bService.handleTimeout(payload)
                .subscribe(
                        b2b -> log.warn("B2B timeout processed: id={}", b2b.getId()),
                        error -> log.error("B2B timeout processing failed", error)
                );

        return Mono.just(ResponseEntity.ok(MpesaAckResponse.accepted()));
    }
}
