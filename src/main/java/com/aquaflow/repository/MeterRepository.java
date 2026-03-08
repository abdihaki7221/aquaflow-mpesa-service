package com.aquaflow.repository;

import com.aquaflow.entity.Meter;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MeterRepository extends R2dbcRepository<Meter, Long> {
    Mono<Meter> findByMeterNumber(String meterNumber);
    Flux<Meter> findByStatus(String status);
    Flux<Meter> findByTenantNameContainingIgnoreCase(String name);
}
