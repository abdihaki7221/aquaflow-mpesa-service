package com.aquaflow.repository;

import com.aquaflow.entity.B2BTransaction;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface B2BTransactionRepository extends R2dbcRepository<B2BTransaction, Long> {
    Mono<B2BTransaction> findByConversationId(String conversationId);
    Flux<B2BTransaction> findByC2bTransactionId(Long c2bTransactionId);
}
