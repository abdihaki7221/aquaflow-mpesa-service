package com.aquaflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("mpesa_c2b_transactions")
public class C2BTransaction {

    @Id
    private Long id;

    @Column("transaction_type")
    private String transactionType;

    @Column("trans_id")
    private String transId;

    @Column("trans_time")
    private String transTime;

    @Column("trans_amount")
    private BigDecimal transAmount;

    @Column("business_shortcode")
    private String businessShortcode;

    @Column("bill_ref_number")
    private String billRefNumber;

    @Column("invoice_number")
    private String invoiceNumber;

    @Column("org_account_balance")
    private BigDecimal orgAccountBalance;

    @Column("third_party_trans_id")
    private String thirdPartyTransId;

    @Column("msisdn")
    private String msisdn;

    @Column("first_name")
    private String firstName;

    @Column("middle_name")
    private String middleName;

    @Column("last_name")
    private String lastName;

    @Column("status")
    private String status;

    @Column("b2b_disbursed")
    private Boolean b2bDisbursed;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
