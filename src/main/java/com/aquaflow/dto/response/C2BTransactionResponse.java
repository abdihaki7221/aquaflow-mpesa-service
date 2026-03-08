package com.aquaflow.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class C2BTransactionResponse {
    private Long id;
    private String transactionType;
    private String transId;
    private BigDecimal transAmount;
    private String billRefNumber;
    private String msisdn;
    private String customerName;
    private String status;
    private Boolean b2bDisbursed;
    private B2BTransactionResponse b2bTransaction;
    private LocalDateTime createdAt;
}
