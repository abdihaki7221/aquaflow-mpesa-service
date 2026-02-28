package com.aquaflow.service;

import com.aquaflow.config.DarajaProperties;
import com.aquaflow.dto.daraja.C2BCallbackPayload;
import com.aquaflow.dto.daraja.C2BRegisterUrlRequest;
import com.aquaflow.dto.daraja.C2BRegisterUrlResponse;
import com.aquaflow.entity.C2BTransaction;
import com.aquaflow.enums.TransactionStatus;
import com.aquaflow.exception.DarajaApiException;
import com.aquaflow.repository.C2BTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class C2BService {

    private final WebClient darajaWebClient;
    private final DarajaProperties props;
    private final DarajaAuthService authService;
    private final C2BTransactionRepository c2bRepository;
    private final B2BService b2bService;

    /**
     * Register C2B Validation and Confirmation URLs with Safaricom.
     * This should be called once (or on application startup).
     */
    public Mono<C2BRegisterUrlResponse> registerUrls() {
        C2BRegisterUrlRequest request = C2BRegisterUrlRequest.builder()
                .shortCode(props.getC2b().getShortcode())
                .responseType(props.getC2b().getResponseType())
                .confirmationURL(props.getC2b().getConfirmationUrl())
                .validationURL(props.getC2b().getValidationUrl())
                .build();

        log.info("Registering C2B URLs: confirmation={}, validation={}",
                request.getConfirmationURL(), request.getValidationURL());

        return authService.getAccessToken()
                .flatMap(token -> darajaWebClient.post()
                        .uri("/mpesa/c2b/v1/registerurl")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .retrieve()
                        .onStatus(status -> status.isError(), response ->
                                response.bodyToMono(String.class)
                                        .flatMap(body -> Mono.error(
                                                new DarajaApiException("C2B URL registration failed: " + body))))
                        .bodyToMono(C2BRegisterUrlResponse.class))
                .doOnNext(resp -> log.info("C2B URL registration response: {}", resp.getResponseDescription()))
                .doOnError(e -> log.error("C2B URL registration failed", e));
    }

    /**
     * Handle Validation callback from Safaricom.
     * Return ResultCode 0 to accept, or 1 to reject the transaction.
     */
    public Mono<C2BTransaction> handleValidation(C2BCallbackPayload payload) {
        log.info("C2B Validation received: transId={}, amount={}, msisdn={}, account={}",
                payload.getTransId(), payload.getTransAmount(),
                payload.getMsisdn(), payload.getBillRefNumber());

        // Idempotency check
        return c2bRepository.existsByTransId(payload.getTransId())
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Duplicate C2B validation for transId={}", payload.getTransId());
                        return c2bRepository.findByTransId(payload.getTransId());
                    }

                    // Add your validation logic here:
                    // e.g., check if account number is valid, amount meets minimum, etc.

                    C2BTransaction transaction = mapToEntity(payload, TransactionStatus.VALIDATED);
                    return c2bRepository.save(transaction);
                })
                .doOnNext(tx -> log.info("C2B transaction validated: id={}, transId={}", tx.getId(), tx.getTransId()))
                .doOnError(e -> log.error("C2B validation processing failed for transId={}", payload.getTransId(), e));
    }

    /**
     * Handle Confirmation callback from Safaricom.
     * This is called after the transaction is completed.
     * Triggers automatic B2B disbursement of 50%.
     */
    public Mono<C2BTransaction> handleConfirmation(C2BCallbackPayload payload) {
        log.info("C2B Confirmation received: transId={}, amount={}, msisdn={}, account={}",
                payload.getTransId(), payload.getTransAmount(),
                payload.getMsisdn(), payload.getBillRefNumber());

        return c2bRepository.findByTransId(payload.getTransId())
                .switchIfEmpty(Mono.defer(() -> {
                    // If validation was skipped or not configured, create new record
                    C2BTransaction transaction = mapToEntity(payload, TransactionStatus.CONFIRMED);
                    return c2bRepository.save(transaction);
                }))
                .flatMap(existing -> {
                    existing.setStatus(TransactionStatus.CONFIRMED.name());
                    existing.setOrgAccountBalance(payload.getOrgAccountBalance());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return c2bRepository.save(existing);
                })
                .flatMap(confirmedTx -> {
                    // Trigger B2B disbursement of configured percentage
                    log.info("Triggering B2B disbursement for C2B transId={}, amount={}",
                            confirmedTx.getTransId(), confirmedTx.getTransAmount());

                    return b2bService.disburseFunds(confirmedTx)
                            .then(Mono.just(confirmedTx))
                            .onErrorResume(e -> {
                                log.error("B2B disbursement failed for C2B transId={}: {}",
                                        confirmedTx.getTransId(), e.getMessage());
                                // Don't fail the confirmation callback if B2B fails
                                // The B2B can be retried separately
                                return Mono.just(confirmedTx);
                            });
                })
                .doOnNext(tx -> log.info("C2B confirmation processed: id={}, transId={}", tx.getId(), tx.getTransId()))
                .doOnError(e -> log.error("C2B confirmation processing failed for transId={}", payload.getTransId(), e));
    }

    private C2BTransaction mapToEntity(C2BCallbackPayload payload, TransactionStatus status) {
        return C2BTransaction.builder()
                .transactionType(payload.getTransactionType())
                .transId(payload.getTransId())
                .transTime(payload.getTransTime())
                .transAmount(payload.getTransAmount())
                .businessShortcode(payload.getBusinessShortCode())
                .billRefNumber(payload.getBillRefNumber())
                .invoiceNumber(payload.getInvoiceNumber())
                .orgAccountBalance(payload.getOrgAccountBalance())
                .thirdPartyTransId(payload.getThirdPartyTransId())
                .msisdn(payload.getMsisdn())
                .firstName(payload.getFirstName())
                .middleName(payload.getMiddleName())
                .lastName(payload.getLastName())
                .status(status.name())
                .b2bDisbursed(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
