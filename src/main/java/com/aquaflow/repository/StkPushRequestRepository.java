package com.aquaflow.repository;

import com.aquaflow.entity.StkPushRequest;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StkPushRequestRepository extends R2dbcRepository<StkPushRequest, Long> {
    Mono<StkPushRequest> findByCheckoutRequestId(String checkoutRequestId);
    Flux<StkPushRequest> findByMeterNumber(String meterNumber);
    Flux<StkPushRequest> findByPhone(String phone);
}
