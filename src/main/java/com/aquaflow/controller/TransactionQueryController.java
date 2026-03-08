package com.aquaflow.controller;

import com.aquaflow.dto.response.ApiResponse;
import com.aquaflow.dto.response.C2BTransactionResponse;
import com.aquaflow.service.TransactionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Query C2B/B2B transactions")
public class TransactionQueryController {
    private final TransactionQueryService queryService;

    @GetMapping("/{transId}")
    @Operation(summary = "Get transaction by Safaricom reference ID")
    public Mono<ApiResponse<C2BTransactionResponse>> byTransId(@PathVariable String transId) {
        return queryService.findByTransId(transId).map(ApiResponse::ok);
    }

    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Get all transactions for an account (meter) number")
    public Mono<ApiResponse<List<C2BTransactionResponse>>> byAccount(@PathVariable String accountNumber) {
        return queryService.findByAccount(accountNumber).collectList().map(ApiResponse::ok);
    }

    @GetMapping("/phone/{msisdn}")
    @Operation(summary = "Get all transactions for a phone number")
    public Mono<ApiResponse<List<C2BTransactionResponse>>> byPhone(@PathVariable String msisdn) {
        return queryService.findByPhone(msisdn).collectList().map(ApiResponse::ok);
    }
}
