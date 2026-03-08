package com.aquaflow.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Table("c2b_transactions")
public class C2BTransaction {
    @Id private Long id;
    private String transactionType;
    private String transId;
    private String transTime;
    private BigDecimal transAmount;
    private String businessShortCode;
    private String billRefNumber;
    private String invoiceNumber;
    private BigDecimal orgAccountBalance;
    private String thirdPartyTransId;
    private String msisdn;
    private String firstName;
    private String middleName;
    private String lastName;
    private String status;
    private Boolean b2bDisbursed;
    private LocalDateTime createdAt;
}
