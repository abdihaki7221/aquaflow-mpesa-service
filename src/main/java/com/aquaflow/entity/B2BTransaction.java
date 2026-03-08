package com.aquaflow.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Table("b2b_transactions")
public class B2BTransaction {
    @Id private Long id;
    private Long c2bTransactionId;
    private String conversationId;
    private String originatorConversationId;
    private BigDecimal amount;
    private String senderShortcode;
    private String receiverShortcode;
    private String status;
    private Integer resultCode;
    private String resultDesc;
    private String transId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
