package com.aquaflow.service;

import com.aquaflow.dto.response.B2BTransactionResponse;
import com.aquaflow.dto.response.C2BTransactionResponse;
import com.aquaflow.entity.B2BTransaction;
import com.aquaflow.entity.C2BTransaction;
import com.aquaflow.exception.TransactionNotFoundException;
import com.aquaflow.repository.B2BTransactionRepository;
import com.aquaflow.repository.C2BTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionQueryService {

    private final C2BTransactionRepository c2bRepository;
    private final B2BTransactionRepository b2bRepository;

    /**
     * Fetch a C2B transaction by Safaricom transaction reference ID (e.g., RKTQDM7W6S).
     * Includes associated B2B disbursement details if available.
     */
    public Mono<C2BTransactionResponse> getByTransactionRef(String transId) {
        log.debug("Fetching transaction by transId={}", transId);

        return c2bRepository.findByTransId(transId)
                .switchIfEmpty(Mono.error(new TransactionNotFoundException(
                        "Transaction not found with reference: " + transId)))
                .flatMap(this::enrichWithB2B);
    }

    /**
     * Fetch all C2B transactions for a given account number (BillRefNumber).
     * This is the account reference the customer entered when paying.
     */
    public Flux<C2BTransactionResponse> getByAccountNumber(String accountNumber) {
        log.debug("Fetching transactions by accountNumber={}", accountNumber);

        return c2bRepository.findAllByBillRefNumber(accountNumber)
                .flatMap(this::enrichWithB2B);
    }

    /**
     * Fetch all C2B transactions for a given phone number.
     */
    public Flux<C2BTransactionResponse> getByPhoneNumber(String msisdn) {
        log.debug("Fetching transactions by msisdn={}", msisdn);

        return c2bRepository.findAllByMsisdn(msisdn)
                .flatMap(this::enrichWithB2B);
    }

    private Mono<C2BTransactionResponse> enrichWithB2B(C2BTransaction c2b) {
        return b2bRepository.findByC2bTransactionId(c2b.getId())
                .map(this::mapB2BResponse)
                .defaultIfEmpty(new B2BTransactionResponse()) // empty placeholder
                .map(b2bResp -> {
                    C2BTransactionResponse response = mapC2BResponse(c2b);
                    if (b2bResp.getId() != null) {
                        response.setB2bTransaction(b2bResp);
                    }
                    return response;
                });
    }

    private C2BTransactionResponse mapC2BResponse(C2BTransaction entity) {
        String customerName = buildCustomerName(entity.getFirstName(), entity.getMiddleName(), entity.getLastName());

        return C2BTransactionResponse.builder()
                .id(entity.getId())
                .transactionType(entity.getTransactionType())
                .transId(entity.getTransId())
                .transTime(entity.getTransTime())
                .transAmount(entity.getTransAmount())
                .businessShortcode(entity.getBusinessShortcode())
                .billRefNumber(entity.getBillRefNumber())
                .msisdn(entity.getMsisdn())
                .customerName(customerName)
                .status(entity.getStatus())
                .b2bDisbursed(entity.getB2bDisbursed())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private B2BTransactionResponse mapB2BResponse(B2BTransaction entity) {
        return B2BTransactionResponse.builder()
                .id(entity.getId())
                .conversationId(entity.getConversationId())
                .originatorConversationId(entity.getOriginatorConversationId())
                .senderShortcode(entity.getSenderShortcode())
                .receiverShortcode(entity.getReceiverShortcode())
                .amount(entity.getAmount())
                .commandId(entity.getCommandId())
                .status(entity.getStatus())
                .resultCode(entity.getResultCode())
                .resultDesc(entity.getResultDesc())
                .transactionId(entity.getTransactionId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String buildCustomerName(String first, String middle, String last) {
        StringBuilder sb = new StringBuilder();
        if (first != null) sb.append(first);
        if (middle != null) sb.append(" ").append(middle);
        if (last != null) sb.append(" ").append(last);
        return sb.toString().trim();
    }
}
