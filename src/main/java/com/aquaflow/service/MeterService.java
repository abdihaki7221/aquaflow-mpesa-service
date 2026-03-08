package com.aquaflow.service;

import com.aquaflow.dto.request.MeterReadingRequest;
import com.aquaflow.entity.Meter;
import com.aquaflow.exception.MeterNotFoundException;
import com.aquaflow.repository.MeterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j @Service @RequiredArgsConstructor
public class MeterService {
    private final MeterRepository meterRepo;

    public Flux<Meter> getAllMeters() { return meterRepo.findAll(); }
    public Flux<Meter> getMetersByStatus(String status) { return meterRepo.findByStatus(status); }
    public Mono<Meter> getMeterByNumber(String meterNumber) {
        return meterRepo.findByMeterNumber(meterNumber)
                .switchIfEmpty(Mono.error(new MeterNotFoundException(meterNumber)));
    }

    public Mono<Meter> recordReading(MeterReadingRequest req) {
        return meterRepo.findByMeterNumber(req.getMeterNumber())
                .switchIfEmpty(Mono.error(new MeterNotFoundException(req.getMeterNumber())))
                .flatMap(meter -> {
                    if (req.getCurrentReading() <= meter.getCurrentReading()) {
                        return Mono.error(new IllegalArgumentException("New reading must be greater than current: " + meter.getCurrentReading()));
                    }
                    meter.setPreviousReading(meter.getCurrentReading());
                    meter.setCurrentReading(req.getCurrentReading());
                    meter.setLastReadDate(LocalDateTime.now());
                    meter.setUpdatedAt(LocalDateTime.now());
                    return meterRepo.save(meter);
                });
    }

    public Mono<Meter> createMeter(Meter meter) {
        meter.setCreatedAt(LocalDateTime.now());
        meter.setUpdatedAt(LocalDateTime.now());
        if (meter.getStatus() == null) meter.setStatus("ACTIVE");
        return meterRepo.save(meter);
    }
}
