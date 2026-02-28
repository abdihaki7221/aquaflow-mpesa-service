package com.aquaflow.repository;

import com.aquaflow.entity.C2BTransaction;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface C2BTransactionRepository extends ReactiveCrudRepository<C2BTransaction, Long> {

    /**
     * Find by Safaricom transaction reference ID (e.g., RKTQDM7W6S).
     */
    Mono<C2BTransaction> findByTransId(String transId);

    /**
     * Find all transactions for a given account number (BillRefNumber).
     * Ordered by most recent first.
     */
    @Query("SELECT * FROM mpesa_c2b_transactions WHERE bill_ref_number = :billRefNumber ORDER BY created_at DESC")
    Flux<C2BTransaction> findAllByBillRefNumber(String billRefNumber);

    /**
     * Find all transactions for a given MSISDN (phone number).
     */
    @Query("SELECT * FROM mpesa_c2b_transactions WHERE msisdn = :msisdn ORDER BY created_at DESC")
    Flux<C2BTransaction> findAllByMsisdn(String msisdn);

    /**
     * Check if a transaction already exists (idempotency guard).
     */
    Mono<Boolean> existsByTransId(String transId);
}
