package com.aquaflow.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("water_bills")
public class WaterBill {

    @Id
    private Long id;

    @Column("meter_id")
    private Long meterId;

    @Column("meter_number")
    private String meterNumber;

    @Column("billing_period")
    private String billingPeriod;

    @Column("previous_reading")
    private Long previousReading;

    @Column("current_reading")
    private Long currentReading;

    @Column("usage_litres")
    private Long usageLitres;

    @Column("units_consumed")
    private BigDecimal unitsConsumed;

    @Column("amount")
    private BigDecimal amount;

    @Column("status")
    private String status;

    @Column("paid_at")
    private LocalDateTime paidAt;

    @Column("mpesa_receipt")
    private String mpesaReceipt;

    @Column("created_at")
    private LocalDateTime createdAt;
}