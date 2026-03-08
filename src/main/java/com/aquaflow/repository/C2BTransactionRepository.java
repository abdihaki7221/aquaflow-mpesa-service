package com.aquaflow.repository;

import com.aquaflow.entity.C2BTransaction;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface C2BTransactionRepository extends R2dbcRepository<C2BTransaction, Long> {
    Mono<C2BTransaction> findByTransId(String transId);
    Flux<C2BTransaction> findByBillRefNumber(String billRefNumber);
    Flux<C2BTransaction> findByMsisdn(String msisdn);
}
