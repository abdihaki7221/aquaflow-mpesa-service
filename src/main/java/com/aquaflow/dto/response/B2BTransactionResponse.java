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
public class B2BTransactionResponse {

    private Long id;
    private String conversationId;
    private String originatorConversationId;
    private String senderShortcode;
    private String receiverShortcode;
    private BigDecimal amount;
    private String commandId;
    private String status;
    private Integer resultCode;
    private String resultDesc;
    private String transactionId;
    private LocalDateTime createdAt;
}
