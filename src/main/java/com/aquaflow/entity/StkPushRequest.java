package com.aquaflow.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Table("stk_push_requests")
public class StkPushRequest {
    @Id private Long id;
    private String merchantRequestId;
    private String checkoutRequestId;
    private String meterNumber;
    private String phone;
    private BigDecimal amount;
    private String accountReference;
    private String description;
    private String status;
    private Integer resultCode;
    private String resultDesc;
    private String mpesaReceiptNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
