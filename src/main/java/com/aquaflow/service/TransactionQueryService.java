package com.aquaflow.service;

import com.aquaflow.dto.response.*;
import com.aquaflow.entity.C2BTransaction;
import com.aquaflow.exception.TransactionNotFoundException;
import com.aquaflow.repository.B2BTransactionRepository;
import com.aquaflow.repository.C2BTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service @RequiredArgsConstructor
public class TransactionQueryService {
    private final C2BTransactionRepository c2bRepo;
    private final B2BTransactionRepository b2bRepo;

    public Mono<C2BTransactionResponse> findByTransId(String transId) {
        return c2bRepo.findByTransId(transId)
                .switchIfEmpty(Mono.error(new TransactionNotFoundException("Transaction not found: " + transId)))
                .flatMap(this::enrichWithB2B);
    }

    public Flux<C2BTransactionResponse> findByAccount(String accountNumber) {
        return c2bRepo.findByBillRefNumber(accountNumber).flatMap(this::enrichWithB2B);
    }

    public Flux<C2BTransactionResponse> findByPhone(String msisdn) {
        return c2bRepo.findByMsisdn(msisdn).flatMap(this::enrichWithB2B);
    }

    private Mono<C2BTransactionResponse> enrichWithB2B(C2BTransaction c2b) {
        return b2bRepo.findByC2bTransactionId(c2b.getId()).next()
                .map(b2b -> C2BTransactionResponse.builder()
                        .id(c2b.getId()).transactionType(c2b.getTransactionType()).transId(c2b.getTransId())
                        .transAmount(c2b.getTransAmount()).billRefNumber(c2b.getBillRefNumber())
                        .msisdn(c2b.getMsisdn())
                        .customerName(String.join(" ", nullSafe(c2b.getFirstName()), nullSafe(c2b.getLastName())).trim())
                        .status(c2b.getStatus()).b2bDisbursed(c2b.getB2bDisbursed())
                        .b2bTransaction(B2BTransactionResponse.builder()
                                .conversationId(b2b.getConversationId()).amount(b2b.getAmount())
                                .status(b2b.getStatus()).receiverShortcode(b2b.getReceiverShortcode())
                                .transId(b2b.getTransId()).build())
                        .createdAt(c2b.getCreatedAt()).build())
                .switchIfEmpty(Mono.just(C2BTransactionResponse.builder()
                        .id(c2b.getId()).transactionType(c2b.getTransactionType()).transId(c2b.getTransId())
                        .transAmount(c2b.getTransAmount()).billRefNumber(c2b.getBillRefNumber())
                        .msisdn(c2b.getMsisdn())
                        .customerName(String.join(" ", nullSafe(c2b.getFirstName()), nullSafe(c2b.getLastName())).trim())
                        .status(c2b.getStatus()).b2bDisbursed(c2b.getB2bDisbursed())
                        .createdAt(c2b.getCreatedAt()).build()));
    }

    private String nullSafe(String s) { return s != null ? s : ""; }
}
