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
@Table("mpesa_b2b_transactions")
public class B2BTransaction {

    @Id
    private Long id;

    @Column("c2b_transaction_id")
    private Long c2bTransactionId;

    @Column("conversation_id")
    private String conversationId;

    @Column("originator_conversation_id")
    private String originatorConversationId;

    @Column("sender_shortcode")
    private String senderShortcode;

    @Column("receiver_shortcode")
    private String receiverShortcode;

    @Column("amount")
    private BigDecimal amount;

    @Column("command_id")
    private String commandId;

    @Column("status")
    private String status;

    @Column("result_type")
    private Integer resultType;

    @Column("result_code")
    private Integer resultCode;

    @Column("result_desc")
    private String resultDesc;

    @Column("transaction_id")
    private String transactionId;

    @Column("debit_account_balance")
    private String debitAccountBalance;

    @Column("credit_account_balance")
    private String creditAccountBalance;

    @Column("transaction_completed_time")
    private String transactionCompletedTime;

    @Column("raw_request")
    private String rawRequest;

    @Column("raw_result")
    private String rawResult;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
