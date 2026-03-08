package com.aquaflow.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Table("water_bills")
public class WaterBill {
    @Id private Long id;
    private Long meterId;
    private String meterNumber;
    private String billingPeriod;
    private Long previousReading;
    private Long currentReading;
    private Long usageLitres;
    private BigDecimal unitsConsumed;
    private BigDecimal amount;
    private String status;
    private LocalDateTime paidAt;
    private String mpesaReceipt;
    private LocalDateTime createdAt;
}
