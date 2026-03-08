package com.aquaflow.repository;

import com.aquaflow.entity.WaterBill;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WaterBillRepository extends R2dbcRepository<WaterBill, Long> {
    Flux<WaterBill> findByMeterNumber(String meterNumber);
    Flux<WaterBill> findByMeterNumberAndStatus(String meterNumber, String status);
    Flux<WaterBill> findByStatus(String status);
    Mono<WaterBill> findByMeterNumberAndBillingPeriod(String meterNumber, String billingPeriod);
}
