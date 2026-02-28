package com.aquaflow.repository;

import com.aquaflow.entity.B2BTransaction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface B2BTransactionRepository extends ReactiveCrudRepository<B2BTransaction, Long> {

    Mono<B2BTransaction> findByConversationId(String conversationId);

    Mono<B2BTransaction> findByOriginatorConversationId(String originatorConversationId);

    Mono<B2BTransaction> findByC2bTransactionId(Long c2bTransactionId);
}
