package com.aquaflow.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StkPushResponseDto {
    private Long id;
    private String checkoutRequestId;
    private String merchantRequestId;
    private String meterNumber;
    private String phone;
    private BigDecimal amount;
    private String status;
    private String mpesaReceiptNumber;
    private LocalDateTime createdAt;
}
