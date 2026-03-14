package com.aquaflow.service;

import com.aquaflow.config.DarajaProperties;
import com.aquaflow.dto.daraja.*;
import com.aquaflow.dto.response.MpesaAckResponse;
import com.aquaflow.entity.C2BTransaction;
import com.aquaflow.exception.DarajaApiException;
import com.aquaflow.repository.C2BTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j @Service @RequiredArgsConstructor
public class C2BService {
    private final DarajaProperties props;
    private final DarajaAuthService authService;
    private final C2BTransactionRepository c2bRepo;
    private final B2BService b2bService;
    private final WaterBillingService billingService;
    private final WebClient.Builder webClientBuilder;

    public Mono<C2BRegisterUrlResponse> registerUrls() {
        return authService.getAccessToken().flatMap(token -> {
            C2BRegisterUrlRequest req = C2BRegisterUrlRequest.builder()
                    .shortCode(props.getC2b().getShortcode())
                    .responseType("Completed")
                    .confirmationURL(props.getC2b().getConfirmationUrl())
                    .validationURL(props.getC2b().getValidationUrl())
                    .build();
            return webClientBuilder.build().post()
                    .uri(props.getBaseUrl() + "/mpesa/c2b/v1/registerurl")
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(req).retrieve().bodyToMono(C2BRegisterUrlResponse.class);
        }).onErrorMap(e -> new DarajaApiException("C2B URL registration failed", e));
    }

    public Mono<MpesaAckResponse> handleValidation(C2BCallbackPayload payload) {
        log.info("C2B Validation: TransID={}, Amount={}, Account={}", payload.getTransID(), payload.getTransAmount(), payload.getBillRefNumber());
        // Save with VALIDATED status
        C2BTransaction txn = C2BTransaction.builder()
                .transactionType(payload.getTransactionType()).transId(payload.getTransID())
                .transTime(payload.getTransTime()).transAmount(payload.getTransAmount())
                .businessShortCode(payload.getBusinessShortCode()).billRefNumber(payload.getBillRefNumber())
                .invoiceNumber(payload.getInvoiceNumber()).orgAccountBalance(payload.getOrgAccountBalance())
                .thirdPartyTransId(payload.getThirdPartyTransID()).msisdn(payload.getMsisdn())
                .firstName(payload.getFirstName()).middleName(payload.getMiddleName()).lastName(payload.getLastName())
                .status("VALIDATED").b2bDisbursed(false).createdAt(LocalDateTime.now()).build();
        return c2bRepo.save(txn).thenReturn(MpesaAckResponse.accepted());
    }

    public Mono<MpesaAckResponse> handleConfirmation(C2BCallbackPayload payload) {
        log.info("========== C2B CONFIRMATION RECEIVED ==========");
        log.info("[C2B] TransID={}, Amount={}, Account={}, Phone={}, Name={} {}",
                payload.getTransID(), payload.getTransAmount(), payload.getBillRefNumber(),
                payload.getMsisdn(), payload.getFirstName(), payload.getLastName());

        return c2bRepo.findByTransId(payload.getTransID())
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("[C2B] Creating new CONFIRMED record: {}", payload.getTransID());

                    C2BTransaction txn = C2BTransaction.builder()
                            .transactionType(payload.getTransactionType())
                            .transId(payload.getTransID())
                            .transTime(payload.getTransTime())
                            .transAmount(payload.getTransAmount())
                            .businessShortCode(payload.getBusinessShortCode())
                            .billRefNumber(payload.getBillRefNumber())
                            .invoiceNumber(payload.getInvoiceNumber())
                            .orgAccountBalance(payload.getOrgAccountBalance())
                            .thirdPartyTransId(payload.getThirdPartyTransID())
                            .msisdn(payload.getMsisdn())
                            .firstName(payload.getFirstName())
                            .middleName(payload.getMiddleName())
                            .lastName(payload.getLastName())
                            .status("CONFIRMED")
                            .b2bDisbursed(false)
                            .createdAt(LocalDateTime.now())
                            .build();

                    return c2bRepo.save(txn);
                }))
                .flatMap(existing -> {
                    if (!"CONFIRMED".equals(existing.getStatus())) {
                        existing.setStatus("CONFIRMED");
                        return c2bRepo.save(existing);
                    }
                    return Mono.just(existing);
                })
                .doOnNext(txn -> {
                    log.info("[C2B] ✅ Transaction saved: id={}, TransID={}, status=CONFIRMED", txn.getId(), txn.getTransId());

                    // BACKGROUND: Mark water bill as paid (non-blocking)
                    billingService.markBillPaid(txn.getBillRefNumber(), txn.getTransId())
                            .subscribe(
                                    bill -> log.info("[C2B] Bill marked paid for account={}", txn.getBillRefNumber()),
                                    err -> log.warn("[C2B] Bill update skipped for account={}: {}", txn.getBillRefNumber(), err.getMessage())
                            );

                    // BACKGROUND: Trigger B2B revenue sharing (non-blocking)
                    log.info("[C2B] >>> Triggering B2B revenue sharing in background for TransID={}", txn.getTransId());
                    b2bService.initiateDisbursement(txn)
                            .subscribe(
                                    unused -> log.info("[C2B] B2B disbursement completed for TransID={}", txn.getTransId()),
                                    err -> log.error("[C2B] B2B disbursement failed for TransID={}: {}", txn.getTransId(), err.getMessage())
                            );
                })
                .thenReturn(MpesaAckResponse.accepted())
                .onErrorResume(e -> {

                    Throwable root = e;
                    while (root.getCause() != null) {
                        root = root.getCause();
                    }

                    log.error("========== SQL ERROR DEBUG ==========");
                    log.error("[C2B] TransID: {}", payload.getTransID());
                    log.error("[C2B] Exception Type: {}", e.getClass().getName());
                    log.error("[C2B] Message{}", String.valueOf(root));
                    log.error("[C2B] Message: {}", e.getMessage());
                    log.error("[C2B] Root Cause Type: {}", root.getClass().getName());
                    log.error("[C2B] Root Cause Message: {}", root.getMessage());


                    log.error("=====================================");

                    return Mono.just(MpesaAckResponse.accepted());
                });
    }
}
