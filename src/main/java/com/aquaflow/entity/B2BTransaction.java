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
@Table("b2b_transactions")
public class B2BTransaction {

    @Id
    private Long id;

    @Column("c2b_transaction_id")
    private Long c2bTransactionId;

    @Column("stk_push_request_id")
    private Long stkPushRequestId;

    @Column("source_type")
    private String sourceType;

    @Column("conversation_id")
    private String conversationId;

    @Column("originator_conversation_id")
    private String originatorConversationId;

    @Column("amount")
    private BigDecimal amount;

    @Column("sender_shortcode")
    private String senderShortcode;

    @Column("receiver_shortcode")
    private String receiverShortcode;

    @Column("account_reference")
    private String accountReference;

    @Column("status")
    private String status;

    @Column("result_code")
    private Integer resultCode;

    @Column("result_desc")
    private String resultDesc;

    @Column("trans_id")
    private String transId;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}