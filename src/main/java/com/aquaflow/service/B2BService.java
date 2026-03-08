package com.aquaflow.service;

import com.aquaflow.config.DarajaProperties;
import com.aquaflow.dto.daraja.*;
import com.aquaflow.entity.B2BTransaction;
import com.aquaflow.entity.C2BTransaction;
import com.aquaflow.exception.DarajaApiException;
import com.aquaflow.repository.B2BTransactionRepository;
import com.aquaflow.repository.C2BTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j @Service @RequiredArgsConstructor
public class B2BService {
    private final DarajaProperties props;
    private final DarajaAuthService authService;
    private final B2BTransactionRepository b2bRepo;
    private final C2BTransactionRepository c2bRepo;
    private final WebClient.Builder webClientBuilder;

    public Mono<Void> initiateDisbursement(C2BTransaction c2bTxn) {
        BigDecimal disbursementAmount = c2bTxn.getTransAmount()
                .multiply(BigDecimal.valueOf(props.getB2b().getDisbursementPercentage()))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);

        return authService.getAccessToken().flatMap(token -> {
            B2BPaymentRequest req = B2BPaymentRequest.builder()
                    .initiator(props.getB2b().getInitiatorName())
                    .securityCredential(props.getB2b().getSecurityCredential())
                    .commandID("BusinessPayBill")
                    .senderIdentifierType("4").recieverIdentifierType("4")
                    .amount(disbursementAmount.toPlainString())
                    .partyA(props.getB2b().getSenderShortcode())
                    .partyB(props.getB2b().getReceiverShortcode())
                    .accountReference("AQUAFLOW-" + c2bTxn.getTransId())
                    .remarks("Water bill disbursement")
                    .queueTimeOutURL(props.getB2b().getQueueTimeoutUrl())
                    .resultURL(props.getB2b().getResultUrl()).build();

            return webClientBuilder.build().post()
                    .uri(props.getBaseUrl() + "/mpesa/b2b/v1/paymentrequest")
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(req).retrieve().bodyToMono(B2BPaymentResponse.class)
                    .flatMap(resp -> {
                        B2BTransaction b2b = B2BTransaction.builder()
                                .c2bTransactionId(c2bTxn.getId())
                                .conversationId(resp.getConversationID())
                                .originatorConversationId(resp.getOriginatorConversationID())
                                .amount(disbursementAmount)
                                .senderShortcode(props.getB2b().getSenderShortcode())
                                .receiverShortcode(props.getB2b().getReceiverShortcode())
                                .status("PENDING").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
                        return b2bRepo.save(b2b).then(c2bRepo.save(c2bTxn).then());
                    });
        }).onErrorMap(e -> new DarajaApiException("B2B disbursement failed", e));
    }

    public Mono<Void> handleResult(B2BResultPayload payload) {
        B2BResultPayload.Result result = payload.getResult();
        log.info("B2B Result: ConvID={}, Code={}", result.getConversationID(), result.getResultCode());
        return b2bRepo.findByConversationId(result.getConversationID())
                .flatMap(b2b -> {
                    b2b.setResultCode(result.getResultCode());
                    b2b.setResultDesc(result.getResultDesc());
                    b2b.setTransId(result.getTransactionID());
                    b2b.setStatus(result.getResultCode() == 0 ? "SUCCESS" : "FAILED");
                    b2b.setUpdatedAt(LocalDateTime.now());
                    return b2bRepo.save(b2b);
                }).then();
    }

    public Mono<Void> handleTimeout(B2BResultPayload payload) {
        log.warn("B2B Timeout: {}", payload);
        if (payload.getResult() != null) {
            return b2bRepo.findByConversationId(payload.getResult().getConversationID())
                    .flatMap(b2b -> { b2b.setStatus("TIMEOUT"); b2b.setUpdatedAt(LocalDateTime.now()); return b2bRepo.save(b2b); }).then();
        }
        return Mono.empty();
    }
}
