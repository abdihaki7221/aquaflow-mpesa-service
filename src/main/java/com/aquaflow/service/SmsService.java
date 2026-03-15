package com.aquaflow.service;

import com.aquaflow.dto.request.SmsNotificationRequest;
import com.aquaflow.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final WebClient.Builder webClientBuilder;

    private static final String FLUXSMS_URL = "https://api.fluxsms.co.ke/sendsms";
    private static final String FLUXSMS_AUTH = "App 7b5cb132bc22a2b748b77c11fbe4b302-3abe3028-a7a4-41ab-8853-895ead25e591";
    private static final String FLUXSMS_API_KEY = "eygqzopcagxsfcbzdusjkatpypzffdnriftgfuau";
    private static final String SENDER_ID = "fluxsms";

    public Mono<String> sendBillNotification(SmsNotificationRequest req) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String dueDate = LocalDate.now().plusDays(14).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String message = String.format(
                "Dear %s,\n" +
                "Your water bill as of %s\n" +
                "Acc No: %s\n" +
                "Prev Read: %d | Curr Read: %d\n" +
                "Consumed: %d units\n" +
                "Total Bill: KSh %s\n" +
                "Pay before: %s\n" +
                "Pay via M-Pesa Paybill: 4010159\n" +
                "Account No: %s\n" +
                "Thank You - AquaFlow",
                req.getTenantName(),
                today,
                req.getMeterNumber(),
                req.getPreviousReading() != null ? req.getPreviousReading() : 0,
                req.getCurrentReading() != null ? req.getCurrentReading() : 0,
                req.getConsumed() != null ? req.getConsumed() : 0,
                req.getAmount().toPlainString(),
                dueDate,
                req.getMeterNumber()
        );

        // Normalize phone: remove spaces/dashes, ensure starts with 0 or 254
        String phone = req.getPhone().replaceAll("[^0-9+]", "");
        if (phone.startsWith("+254")) phone = "0" + phone.substring(4);
        else if (phone.startsWith("254")) phone = "0" + phone.substring(3);

        String finalPhone = phone;
        log.info("[SMS] Sending bill notification to {} for meter {} | Amount: KSh {}",
                finalPhone, req.getMeterNumber(), req.getAmount());
        log.debug("[SMS] Message content:\n{}", message);

        Map<String, String> body = Map.of(
                "api_key", FLUXSMS_API_KEY,
                "message", message,
                "phone", finalPhone,
                "sender_id", SENDER_ID
        );

        return webClientBuilder.build().post()
                .uri(FLUXSMS_URL)
                .header("Authorization", FLUXSMS_AUTH)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(resp -> log.info("[SMS] ✅ FluxSMS response for {}: {}", finalPhone, resp))
                .onErrorResume(e -> {
                    log.error("[SMS] ❌ Failed to send SMS to {}: {}", finalPhone, e.getMessage());
                    return Mono.just("SMS_FAILED: " + e.getMessage());
                });
    }
}
