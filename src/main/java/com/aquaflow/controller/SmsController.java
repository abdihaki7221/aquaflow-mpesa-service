package com.aquaflow.controller;

import com.aquaflow.dto.request.SmsNotificationRequest;
import com.aquaflow.dto.response.ApiResponse;
import com.aquaflow.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
@Tag(name = "SMS Notifications", description = "Send bill notifications via SMS")
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/notify")
    @Operation(summary = "Send water bill SMS notification to a tenant")
    public Mono<ApiResponse<String>> sendNotification(@Valid @RequestBody SmsNotificationRequest req) {
        log.info("[SMS-Controller] Notification request: phone={}, meter={}, amount={}",
                req.getPhone(), req.getMeterNumber(), req.getAmount());
        return smsService.sendBillNotification(req)
                .map(result -> {
                    if (result.startsWith("SMS_FAILED")) {
                        return ApiResponse.<String>builder()
                                .success(false).message("SMS delivery failed").data(result).build();
                    }
                    return ApiResponse.ok("SMS notification sent successfully", result);
                });
    }
}
