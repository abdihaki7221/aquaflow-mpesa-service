package com.aquaflow.dto.response;
import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class B2BTransactionResponse {
    private String conversationId;
    private BigDecimal amount;
    private String status;
    private String receiverShortcode;
    private String transId;
}
