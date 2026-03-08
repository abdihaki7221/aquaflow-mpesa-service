package com.aquaflow.service;

import com.aquaflow.config.DarajaProperties;
import com.aquaflow.dto.daraja.*;
import com.aquaflow.dto.request.StkPushRequestDto;
import com.aquaflow.dto.response.StkPushResponseDto;
import com.aquaflow.entity.StkPushRequest;
import com.aquaflow.exception.DarajaApiException;
import com.aquaflow.repository.StkPushRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Slf4j @Service @RequiredArgsConstructor
public class StkPushService {
    private final DarajaProperties props;
    private final DarajaAuthService authService;
    private final StkPushRequestRepository stkRepo;
    private final WaterBillingService billingService;

    private final WebClient.Builder webClientBuilder;

    private String generatePassword(String timestamp) {
        String raw = props.getStk().getShortcode() + props.getStk().getPasskey() + timestamp;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public Mono<StkPushResponseDto> initiatePayment(StkPushRequestDto req) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String password = generatePassword(timestamp);
        String phone = req.getPhone().replaceAll("[^0-9]", "");
        if (phone.startsWith("0")) phone = "254" + phone.substring(1);
        if (phone.startsWith("+")) phone = phone.substring(1);

        String finalPhone = phone;
        return authService.getAccessToken().flatMap(token -> {
            StkPushRequestPayload payload = StkPushRequestPayload.builder()
                    .businessShortCode(props.getStk().getShortcode())
                    .password(password).timestamp(timestamp)
                    .transactionType("CustomerPayBillOnline")
                    .amount(req.getAmount().setScale(0).toPlainString())
                    .partyA(finalPhone).partyB(props.getStk().getShortcode())
                    .phoneNumber(finalPhone)
                    .callBackURL(props.getStk().getCallbackUrl())
                    .accountReference(req.getMeterNumber())
                    .transactionDesc(req.getDescription() != null ? req.getDescription() : "Water bill payment for " + req.getMeterNumber())
                    .build();

            return webClientBuilder.build().post()
                    .uri(props.getBaseUrl() + "/mpesa/stkpush/v1/processrequest")
                    .header("Authorization", "Bearer " + token)
                    .bodyValue(payload).retrieve().bodyToMono(StkPushResponse.class)
                    .flatMap(resp -> {
                        StkPushRequest entity = StkPushRequest.builder()
                                .merchantRequestId(resp.getMerchantRequestID())
                                .checkoutRequestId(resp.getCheckoutRequestID())
                                .meterNumber(req.getMeterNumber())
                                .phone(finalPhone).amount(req.getAmount())
                                .accountReference(req.getMeterNumber())
                                .description(payload.getTransactionDesc())
                                .status("PENDING")
                                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
                        return stkRepo.save(entity).map(saved -> StkPushResponseDto.builder()
                                .id(saved.getId()).checkoutRequestId(saved.getCheckoutRequestId())
                                .merchantRequestId(saved.getMerchantRequestId())
                                .meterNumber(saved.getMeterNumber()).phone(saved.getPhone())
                                .amount(saved.getAmount()).status(saved.getStatus())
                                .createdAt(saved.getCreatedAt()).build());
                    });
        }).onErrorMap(e -> !(e instanceof DarajaApiException) ? new DarajaApiException("STK push failed", e) : e);
    }

    public Mono<Void> handleCallback(StkCallbackPayload payload) {
        StkCallbackPayload.StkCallback cb = payload.getBody().getStkCallback();
        log.info("STK Callback: CheckoutReqID={}, ResultCode={}", cb.getCheckoutRequestID(), cb.getResultCode());

        return stkRepo.findByCheckoutRequestId(cb.getCheckoutRequestID())
                .flatMap(req -> {
                    req.setResultCode(cb.getResultCode());
                    req.setResultDesc(cb.getResultDesc());
                    req.setUpdatedAt(LocalDateTime.now());

                    if (cb.getResultCode() == 0) {
                        req.setStatus("SUCCESS");
                        // Extract receipt number from callback metadata
                        if (cb.getCallbackMetadata() != null && cb.getCallbackMetadata().getItem() != null) {
                            for (StkCallbackPayload.Item item : cb.getCallbackMetadata().getItem()) {
                                if ("MpesaReceiptNumber".equals(item.getName())) {
                                    req.setMpesaReceiptNumber(String.valueOf(item.getValue()));
                                }
                            }
                        }
                        return stkRepo.save(req)
                                .flatMap(saved -> billingService.markBillPaid(saved.getMeterNumber(), saved.getMpesaReceiptNumber())
                                        .then().onErrorResume(e -> Mono.empty()));
                    } else {
                        req.setStatus(cb.getResultCode() == 1032 ? "CANCELLED" : "FAILED");
                        return stkRepo.save(req).then();
                    }
                }).then();
    }

    public Flux<StkPushResponseDto> getByMeter(String meterNumber) {
        return stkRepo.findByMeterNumber(meterNumber).map(this::toDto);
    }

    public Mono<StkPushResponseDto> getByCheckoutId(String checkoutRequestId) {
        return stkRepo.findByCheckoutRequestId(checkoutRequestId).map(this::toDto);
    }

    private StkPushResponseDto toDto(StkPushRequest e) {
        return StkPushResponseDto.builder()
                .id(e.getId()).checkoutRequestId(e.getCheckoutRequestId())
                .merchantRequestId(e.getMerchantRequestId())
                .meterNumber(e.getMeterNumber()).phone(e.getPhone())
                .amount(e.getAmount()).status(e.getStatus())
                .mpesaReceiptNumber(e.getMpesaReceiptNumber())
                .createdAt(e.getCreatedAt()).build();
    }
}
