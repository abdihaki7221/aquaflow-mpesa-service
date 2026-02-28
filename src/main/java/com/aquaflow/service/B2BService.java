package com.aquaflow.service;

import com.aquaflow.config.DarajaProperties;
import com.aquaflow.dto.daraja.B2BPaymentRequest;
import com.aquaflow.dto.daraja.B2BPaymentResponse;
import com.aquaflow.dto.daraja.B2BResultPayload;
import com.aquaflow.entity.B2BTransaction;
import com.aquaflow.entity.C2BTransaction;
import com.aquaflow.enums.B2BTransactionStatus;
import com.aquaflow.exception.DarajaApiException;
import com.aquaflow.repository.B2BTransactionRepository;
import com.aquaflow.repository.C2BTransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class B2BService {

    private final WebClient darajaWebClient;
    private final DarajaProperties props;
    private final DarajaAuthService authService;
    private final B2BTransactionRepository b2bRepository;
    private final C2BTransactionRepository c2bRepository;
    private final ObjectMapper objectMapper;

    /**
     * Disburse a percentage of the C2B transaction amount via B2B to another business.
     */
    public Mono<B2BTransaction> disburseFunds(C2BTransaction c2bTransaction) {
        BigDecimal percentage = BigDecimal.valueOf(props.getB2b().getDisbursementPercentage());
        BigDecimal disbursementAmount = c2bTransaction.getTransAmount()
                .multiply(percentage)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);

        if (disbursementAmount.compareTo(BigDecimal.ONE) < 0) {
            log.warn("B2B disbursement amount too small ({}) for C2B transId={}. Skipping.",
                    disbursementAmount, c2bTransaction.getTransId());
            return Mono.empty();
        }

        log.info("Initiating B2B disbursement: amount={} ({}% of {}), sender={}, receiver={}",
                disbursementAmount, props.getB2b().getDisbursementPercentage(),
                c2bTransaction.getTransAmount(),
                props.getB2b().getSenderShortcode(),
                props.getB2b().getReceiverShortcode());

        B2BPaymentRequest request = B2BPaymentRequest.builder()
                .initiator(props.getB2b().getInitiatorName())
                .securityCredential(props.getB2b().getSecurityCredential())
                .commandId(props.getB2b().getCommandId())
                .senderIdentifierType("4")   // 4 = Organization shortcode
                .receiverIdentifierType("4")
                .amount(disbursementAmount.longValue())
                .partyA(props.getB2b().getSenderShortcode())
                .partyB(props.getB2b().getReceiverShortcode())
                .accountReference("AquaFlow-" + c2bTransaction.getTransId())
                .remarks("Auto disbursement for C2B " + c2bTransaction.getTransId())
                .queueTimeOutURL(props.getB2b().getQueueTimeoutUrl())
                .resultURL(props.getB2b().getResultUrl())
                .build();

        String rawRequest;
        try {
            rawRequest = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            rawRequest = request.toString();
        }

        final String requestJson = rawRequest;

        return authService.getAccessToken()
                .flatMap(token -> darajaWebClient.post()
                        .uri("/mpesa/b2b/v1/paymentrequest")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .retrieve()
                        .onStatus(status -> status.isError(), response ->
                                response.bodyToMono(String.class)
                                        .flatMap(body -> Mono.error(
                                                new DarajaApiException("B2B payment request failed: " + body))))
                        .bodyToMono(B2BPaymentResponse.class))
                .flatMap(response -> {
                    log.info("B2B payment initiated: conversationId={}, responseCode={}, desc={}",
                            response.getConversationId(),
                            response.getResponseCode(),
                            response.getResponseDescription());

                    B2BTransaction b2bTx = B2BTransaction.builder()
                            .c2bTransactionId(c2bTransaction.getId())
                            .conversationId(response.getConversationId())
                            .originatorConversationId(response.getOriginatorConversationId())
                            .senderShortcode(props.getB2b().getSenderShortcode())
                            .receiverShortcode(props.getB2b().getReceiverShortcode())
                            .amount(disbursementAmount)
                            .commandId(props.getB2b().getCommandId())
                            .status(B2BTransactionStatus.INITIATED.name())
                            .rawRequest(requestJson)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return b2bRepository.save(b2bTx);
                })
                .flatMap(savedB2b -> {
                    // Mark the C2B transaction as having been disbursed
                    c2bTransaction.setB2bDisbursed(true);
                    c2bTransaction.setUpdatedAt(LocalDateTime.now());
                    return c2bRepository.save(c2bTransaction)
                            .thenReturn(savedB2b);
                })
                .doOnNext(b2b -> log.info("B2B transaction saved: id={}, conversationId={}",
                        b2b.getId(), b2b.getConversationId()))
                .doOnError(e -> log.error("B2B disbursement failed for C2B id={}", c2bTransaction.getId(), e));
    }

    /**
     * Handle B2B result callback from Safaricom.
     */
    public Mono<B2BTransaction> handleResult(B2BResultPayload payload) {
        B2BResultPayload.Result result = payload.getResult();
        log.info("B2B Result received: conversationId={}, resultCode={}, resultDesc={}",
                result.getConversationId(), result.getResultCode(), result.getResultDesc());

        String rawResult;
        try {
            rawResult = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            rawResult = payload.toString();
        }

        final String resultJson = rawResult;

        return b2bRepository.findByConversationId(result.getConversationId())
                .switchIfEmpty(b2bRepository.findByOriginatorConversationId(result.getOriginatorConversationId()))
                .switchIfEmpty(Mono.error(new DarajaApiException(
                        "No B2B transaction found for conversationId=" + result.getConversationId())))
                .flatMap(b2bTx -> {
                    b2bTx.setResultType(result.getResultType());
                    b2bTx.setResultCode(result.getResultCode());
                    b2bTx.setResultDesc(result.getResultDesc());
                    b2bTx.setTransactionId(result.getTransactionId());
                    b2bTx.setRawResult(resultJson);
                    b2bTx.setUpdatedAt(LocalDateTime.now());

                    if (result.getResultCode() != null && result.getResultCode() == 0) {
                        b2bTx.setStatus(B2BTransactionStatus.SUCCESS.name());
                        // Extract result parameters
                        extractResultParams(result, b2bTx);
                    } else {
                        b2bTx.setStatus(B2BTransactionStatus.FAILED.name());
                    }

                    return b2bRepository.save(b2bTx);
                })
                .doOnNext(b2b -> log.info("B2B result processed: id={}, status={}, transactionId={}",
                        b2b.getId(), b2b.getStatus(), b2b.getTransactionId()))
                .doOnError(e -> log.error("B2B result processing failed", e));
    }

    /**
     * Handle B2B timeout callback from Safaricom.
     */
    public Mono<B2BTransaction> handleTimeout(B2BResultPayload payload) {
        B2BResultPayload.Result result = payload.getResult();
        log.warn("B2B Timeout received: conversationId={}", result.getConversationId());

        return b2bRepository.findByConversationId(result.getConversationId())
                .switchIfEmpty(b2bRepository.findByOriginatorConversationId(result.getOriginatorConversationId()))
                .flatMap(b2bTx -> {
                    b2bTx.setStatus(B2BTransactionStatus.TIMEOUT.name());
                    b2bTx.setResultCode(result.getResultCode());
                    b2bTx.setResultDesc(result.getResultDesc());
                    b2bTx.setUpdatedAt(LocalDateTime.now());
                    return b2bRepository.save(b2bTx);
                })
                .doOnNext(b2b -> log.warn("B2B timeout processed: id={}", b2b.getId()));
    }

    private void extractResultParams(B2BResultPayload.Result result, B2BTransaction b2bTx) {
        if (result.getResultParameters() == null || result.getResultParameters().getResultParameter() == null) {
            return;
        }

        List<B2BResultPayload.ResultParameter> params = result.getResultParameters().getResultParameter();
        for (B2BResultPayload.ResultParameter param : params) {
            switch (param.getKey()) {
                case "DebitAccountBalance" -> b2bTx.setDebitAccountBalance(String.valueOf(param.getValue()));
                case "CreditAccountBalance" -> b2bTx.setCreditAccountBalance(String.valueOf(param.getValue()));
                case "TransCompletedTime" -> b2bTx.setTransactionCompletedTime(String.valueOf(param.getValue()));
                default -> log.debug("B2B result param: {}={}", param.getKey(), param.getValue());
            }
        }
    }
}
