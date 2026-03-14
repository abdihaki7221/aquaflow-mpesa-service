package com.aquaflow.service;

import com.aquaflow.config.DarajaProperties;
import com.aquaflow.dto.daraja.*;
import com.aquaflow.entity.B2BTransaction;
import com.aquaflow.entity.C2BTransaction;
import com.aquaflow.entity.StkPushRequest;
import com.aquaflow.exception.DarajaApiException;
import com.aquaflow.repository.B2BTransactionRepository;
import com.aquaflow.repository.C2BTransactionRepository;
import com.aquaflow.repository.StkPushRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class B2BService {

    private final DarajaProperties props;
    private final DarajaAuthService authService;
    private final B2BTransactionRepository b2bRepo;
    private final C2BTransactionRepository c2bRepo;
    private final StkPushRequestRepository stkRepo;
    private final WebClient.Builder webClientBuilder;

    // ================================================================
    // Revenue sharing triggered by STK Push callback (50% disbursement)
    // This runs in the BACKGROUND after the STK callback response is sent
    // ================================================================
    public void triggerRevenueShareFromStk(StkPushRequest stkPush) {
        log.info("========== REVENUE SHARING TRIGGERED ==========");
        log.info("[B2B] Source: STK Push | StkPushId={} | Meter={} | Phone={} | Receipt={}",
                stkPush.getId(), stkPush.getMeterNumber(), stkPush.getPhone(), stkPush.getMpesaReceiptNumber());
        log.info("[B2B] Payment amount: KSh {} | Disbursement: {}% to shortcode {}",
                stkPush.getAmount(), props.getB2b().getDisbursementPercentage(), props.getB2b().getReceiverShortcode());

        initiateB2BFromStk(stkPush)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        b2b -> log.info("[B2B] ✅ Revenue share initiated successfully | B2BId={} | ConversationId={} | Amount=KSh {}",
                                b2b.getId(), b2b.getConversationId(), b2b.getAmount()),
                        error -> log.error("[B2B] ❌ Revenue share FAILED for StkPushId={} | Error: {}",
                                stkPush.getId(), error.getMessage(), error)
                );
    }

    private Mono<B2BTransaction> initiateB2BFromStk(StkPushRequest stkPush) {
        BigDecimal disbursementAmount = stkPush.getAmount()
                .multiply(BigDecimal.valueOf(props.getB2b().getDisbursementPercentage()))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);

        String accountRef = "AQUAFLOW-STK-" + stkPush.getMeterNumber() + "-" + stkPush.getMpesaReceiptNumber();

        log.info("[B2B] Calculating disbursement: {} × {}% = KSh {}",
                stkPush.getAmount(), props.getB2b().getDisbursementPercentage(), disbursementAmount);
        log.info("[B2B] Route: {} → {} | AccountRef: {}", 
                props.getB2b().getSenderShortcode(), props.getB2b().getReceiverShortcode(), accountRef);

        return authService.getAccessToken()
                .doOnNext(token -> log.info("[B2B] Daraja auth token acquired, initiating B2B payment request..."))
                .flatMap(token -> {
                    B2BPaymentRequest req = B2BPaymentRequest.builder()
                            .initiator(props.getB2b().getInitiatorName())
                            .securityCredential(props.getB2b().getSecurityCredential())
                            .commandID("BusinessPayment")
                            .senderIdentifierType("4")
                            .recieverIdentifierType("2")
                            .amount(disbursementAmount.toPlainString())
                            .partyA(props.getB2b().getSenderShortcode())
                            .partyB(props.getB2b().getReceiverShortcode())
                            .accountReference(accountRef)
                            .remarks("AquaFlow water bill revenue share - " + stkPush.getMeterNumber())
                            .queueTimeOutURL(props.getB2b().getQueueTimeoutUrl())
                            .resultURL(props.getB2b().getResultUrl())
                            .build();

                    log.info("[B2B] Sending B2B payment request to Daraja API: POST {}/mpesa/b2b/v1/paymentrequest",
                            props.getBaseUrl());
                    log.debug("[B2B] Request payload: Initiator={}, CommandID={}, PartyA={}, PartyB={}, Amount={}",
                            req.getInitiator(), req.getCommandID(), req.getPartyA(), req.getPartyB(), req.getAmount());

                    return webClientBuilder.build().post()
                            .uri(props.getBaseUrl() + "/mpesa/b2b/v1/paymentrequest")
                            .header("Authorization", "Bearer " + token)
                            .bodyValue(req)
                            .retrieve()
                            .bodyToMono(B2BPaymentResponse.class)
                            .doOnNext(resp -> log.info("[B2B] Daraja API response: ConversationID={}, ResponseCode={}, ResponseDesc={}",
                                    resp.getConversationID(), resp.getResponseCode(), resp.getResponseDescription()))
                            .flatMap(resp -> {
                                // Save the B2B transaction record
                                B2BTransaction b2b = B2BTransaction.builder()
                                        .stkPushRequestId(stkPush.getId())
                                        .sourceType("STK")
                                        .conversationId(resp.getConversationID())
                                        .originatorConversationId(resp.getOriginatorConversationID())
                                        .amount(disbursementAmount)
                                        .senderShortcode(props.getB2b().getSenderShortcode())
                                        .receiverShortcode(props.getB2b().getReceiverShortcode())
                                        .accountReference(accountRef)
                                        .status("PENDING")
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                                log.info("[B2B] Saving B2B transaction record: ConvId={}, Amount={}, Status=PENDING",
                                        b2b.getConversationId(), b2b.getAmount());

                                //send sms for meter successfull payment
                                return b2bRepo.save(b2b)
                                        .flatMap(savedB2b -> {
                                            // Update STK push request to mark it as disbursed
                                            stkPush.setB2bDisbursed(true);
                                            stkPush.setB2bTransactionId(savedB2b.getId());
                                            log.info("[B2B] Updating STK push record: StkId={}, b2bDisbursed=true, b2bTransactionId={}",
                                                    stkPush.getId(), savedB2b.getId());
                                            return stkRepo.save(stkPush).thenReturn(savedB2b);
                                        });
                            })
                            .onErrorResume(WebClientResponseException.class, e -> {
                                log.error("[B2B] Daraja API HTTP error: status={}, body={}",
                                        e.getStatusCode(), e.getResponseBodyAsString());
                                // Still save a FAILED record for audit
                                B2BTransaction failedB2b = B2BTransaction.builder()
                                        .stkPushRequestId(stkPush.getId())
                                        .sourceType("STK")
                                        .amount(disbursementAmount)
                                        .senderShortcode(props.getB2b().getSenderShortcode())
                                        .receiverShortcode(props.getB2b().getReceiverShortcode())
                                        .accountReference(accountRef)
                                        .status("FAILED")
                                        .resultDesc("Daraja API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString().substring(0, Math.min(250, e.getResponseBodyAsString().length())))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                                return b2bRepo.save(failedB2b);
                            });
                })
                .onErrorResume(e -> {
                    log.error("[B2B] Failed to initiate revenue sharing: {}", e.getMessage(), e);
                    // Save failed attempt for audit trail
                    B2BTransaction failedB2b = B2BTransaction.builder()
                            .stkPushRequestId(stkPush.getId())
                            .sourceType("STK")
                            .amount(disbursementAmount)
                            .senderShortcode(props.getB2b().getSenderShortcode())
                            .receiverShortcode(props.getB2b().getReceiverShortcode())
                            .accountReference(accountRef)
                            .status("FAILED")
                            .resultDesc("Init error: " + e.getMessage())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return b2bRepo.save(failedB2b);
                });
    }

    // ================================================================
    // Revenue sharing triggered by C2B confirmation (existing flow)
    // ================================================================
    public Mono<Void> initiateDisbursement(C2BTransaction c2bTxn) {
        BigDecimal disbursementAmount = c2bTxn.getTransAmount()
                .multiply(BigDecimal.valueOf(props.getB2b().getDisbursementPercentage()))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);

        String accountRef = "AQUAFLOW-C2B-" + c2bTxn.getTransId();

        log.info("[B2B-C2B] Revenue sharing from C2B: TransId={}, Amount={}, Disbursement={}",
                c2bTxn.getTransId(), c2bTxn.getTransAmount(), disbursementAmount);

        return authService.getAccessToken().flatMap(token -> {
            B2BPaymentRequest req = B2BPaymentRequest.builder()
                    .initiator(props.getB2b().getInitiatorName())
                    .securityCredential(props.getB2b().getSecurityCredential())
                    .commandID("BusinessPayment")
                    .senderIdentifierType("4").recieverIdentifierType("2")
                    .amount(disbursementAmount.toPlainString())
                    .partyA(props.getB2b().getSenderShortcode())
                    .partyB(props.getB2b().getReceiverShortcode())
                    .accountReference(accountRef)
                    .remarks("AquaFlow C2B revenue share - " + c2bTxn.getBillRefNumber())
                    .queueTimeOutURL(props.getB2b().getQueueTimeoutUrl())
                    .resultURL(props.getB2b().getResultUrl()).build();

            return webClientBuilder.build().post()
                    .uri(props.getBaseUrl() + "/mpesa/b2b/v1/paymentrequest")
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(req).retrieve().bodyToMono(B2BPaymentResponse.class)
                    .flatMap(resp -> {
                        B2BTransaction b2b = B2BTransaction.builder()
                                .c2bTransactionId(c2bTxn.getId())
                                .sourceType("C2B")
                                .conversationId(resp.getConversationID())
                                .originatorConversationId(resp.getOriginatorConversationID())
                                .amount(disbursementAmount)
                                .senderShortcode(props.getB2b().getSenderShortcode())
                                .receiverShortcode(props.getB2b().getReceiverShortcode())
                                .accountReference(accountRef)
                                .status("PENDING")
                                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
                        c2bTxn.setB2bDisbursed(true);
                        return b2bRepo.save(b2b).then(c2bRepo.save(c2bTxn).then());
                    });
        }).onErrorMap(e -> new DarajaApiException("B2B disbursement failed", e));
    }

    // ================================================================
    // B2B Result callback handler (from Safaricom)
    // ================================================================
    public Mono<Void> handleResult(B2BResultPayload payload) {
        B2BResultPayload.Result result = payload.getResult();
        log.info("========== B2B RESULT CALLBACK RECEIVED ==========");
        log.info("[B2B-Result] ConversationID={}, ResultCode={}, ResultDesc={}, TransactionID={}",
                result.getConversationID(), result.getResultCode(), result.getResultDesc(), result.getTransactionID());

        return b2bRepo.findByConversationId(result.getConversationID())
                .flatMap(b2b -> {
                    String previousStatus = b2b.getStatus();
                    b2b.setResultCode(result.getResultCode());
                    b2b.setResultDesc(result.getResultDesc());
                    b2b.setTransId(result.getTransactionID());
                    boolean success = "0".equals(result.getResultCode());

                    b2b.setStatus(success ? "SUCCESS" : "FAILED");
                    b2b.setUpdatedAt(LocalDateTime.now());

                    log.info("[B2B-Result] Updating B2B record: B2BId={}, Source={}, PreviousStatus={}, NewStatus={}, Amount=KSh {}",
                            b2b.getId(), b2b.getSourceType(), previousStatus, b2b.getStatus(), b2b.getAmount());

                    if (result.getResultCode().equals("0")) {
                        log.info("[B2B-Result] ✅ REVENUE SHARE SUCCESSFUL | {} → {} | Amount=KSh {} | MpesaTxnId={}",
                                b2b.getSenderShortcode(), b2b.getReceiverShortcode(), b2b.getAmount(), result.getTransactionID());
                    } else {
                        log.warn("[B2B-Result] ❌ REVENUE SHARE FAILED | Code={} | Desc={}",
                                result.getResultCode(), result.getResultDesc());
                    }

                    return b2bRepo.save(b2b);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("[B2B-Result] No B2B transaction found for ConversationID={}", result.getConversationID());
                    return Mono.empty();
                }))
                .then();
    }

    // ================================================================
    // B2B Timeout callback handler
    // ================================================================
    public Mono<Void> handleTimeout(B2BResultPayload payload) {
        log.warn("========== B2B TIMEOUT CALLBACK RECEIVED ==========");
        if (payload.getResult() != null) {
            log.warn("[B2B-Timeout] ConversationID={}", payload.getResult().getConversationID());
            return b2bRepo.findByConversationId(payload.getResult().getConversationID())
                    .flatMap(b2b -> {
                        b2b.setStatus("TIMEOUT");
                        b2b.setResultDesc("B2B payment request timed out");
                        b2b.setUpdatedAt(LocalDateTime.now());
                        log.warn("[B2B-Timeout] Marking B2B as TIMEOUT: B2BId={}, Amount=KSh {}", b2b.getId(), b2b.getAmount());
                        return b2bRepo.save(b2b);
                    }).then();
        }
        return Mono.empty();
    }
}
