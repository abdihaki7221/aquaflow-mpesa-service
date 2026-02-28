package com.aquaflow.controller;

import com.aquaflow.dto.response.ApiResponse;
import com.aquaflow.dto.response.C2BTransactionResponse;
import com.aquaflow.service.TransactionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Query M-Pesa transactions by reference ID or account number")
public class TransactionQueryController {

    private final TransactionQueryService queryService;

    /**
     * Get a single transaction by Safaricom transaction reference ID.
     * Example: GET /api/v1/transactions/RKTQDM7W6S
     */
    @GetMapping("/{transId}")
    @Operation(
            summary = "Get transaction by reference ID",
            description = "Fetch a C2B transaction by its Safaricom transaction ID (e.g., RKTQDM7W6S). " +
                    "Includes B2B disbursement details if available."
    )
    public Mono<ResponseEntity<ApiResponse<C2BTransactionResponse>>> getByTransId(
            @Parameter(description = "Safaricom transaction reference ID", example = "RKTQDM7W6S")
            @PathVariable String transId) {

        return queryService.getByTransactionRef(transId)
                .map(tx -> ResponseEntity.ok(ApiResponse.ok(tx, "Transaction found")));
    }

    /**
     * Get all transactions for a given account number (BillRefNumber).
     * This is the account reference the customer entered when paying on M-Pesa.
     * Example: GET /api/v1/transactions/account/ACC001
     */
    @GetMapping("/account/{accountNumber}")
    @Operation(
            summary = "Get transactions by account number",
            description = "Fetch all C2B transactions for a given account number (BillRefNumber). " +
                    "Returns transactions ordered by most recent first."
    )
    public Mono<ResponseEntity<ApiResponse<List<C2BTransactionResponse>>>> getByAccountNumber(
            @Parameter(description = "Account number / Bill Reference Number", example = "ACC001")
            @PathVariable String accountNumber) {

        return queryService.getByAccountNumber(accountNumber)
                .collectList()
                .map(txs -> {
                    if (txs.isEmpty()) {
                        return ResponseEntity.ok(
                                ApiResponse.<List<C2BTransactionResponse>>ok(txs, "No transactions found for account: " + accountNumber));
                    }
                    return ResponseEntity.ok(ApiResponse.ok(txs, txs.size() + " transaction(s) found"));
                });
    }

    /**
     * Get all transactions for a given phone number (MSISDN).
     * Example: GET /api/v1/transactions/phone/254712345678
     */
    @GetMapping("/phone/{msisdn}")
    @Operation(
            summary = "Get transactions by phone number",
            description = "Fetch all C2B transactions for a given phone number (MSISDN format: 254XXXXXXXXX)"
    )
    public Mono<ResponseEntity<ApiResponse<List<C2BTransactionResponse>>>> getByPhoneNumber(
            @Parameter(description = "Phone number in MSISDN format", example = "254712345678")
            @PathVariable String msisdn) {

        return queryService.getByPhoneNumber(msisdn)
                .collectList()
                .map(txs -> ResponseEntity.ok(ApiResponse.ok(txs, txs.size() + " transaction(s) found")));
    }
}
