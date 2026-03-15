package com.aquaflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SmsNotificationRequest {
    @NotBlank private String phone;
    @NotBlank private String meterNumber;
    @NotBlank private String tenantName;
    private Long previousReading;
    private Long currentReading;
    private Long consumed;
    @Positive private BigDecimal amount;
}
