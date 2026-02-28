package com.aquaflow.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class C2BTransactionResponse {

    private Long id;
    private String transactionType;
    private String transId;
    private String transTime;
    private BigDecimal transAmount;
    private String businessShortcode;
    private String billRefNumber;
    private String msisdn;
    private String customerName;
    private String status;
    private Boolean b2bDisbursed;
    private B2BTransactionResponse b2bTransaction;
    private LocalDateTime createdAt;
}
