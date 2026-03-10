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
@Table("stk_push_requests")
public class StkPushRequest {

    @Id
    private Long id;

    @Column("merchant_request_id")
    private String merchantRequestId;

    @Column("checkout_request_id")
    private String checkoutRequestId;

    @Column("meter_number")
    private String meterNumber;

    @Column("phone")
    private String phone;

    @Column("amount")
    private BigDecimal amount;

    @Column("account_reference")
    private String accountReference;

    @Column("description")
    private String description;

    @Column("status")
    private String status;

    @Column("result_code")
    private Integer resultCode;

    @Column("result_desc")
    private String resultDesc;

    @Column("mpesa_receipt_number")
    private String mpesaReceiptNumber;

    @Column("b2b_disbursed")
    private Boolean b2bDisbursed;

    @Column("b2b_transaction_id")
    private Long b2bTransactionId;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}