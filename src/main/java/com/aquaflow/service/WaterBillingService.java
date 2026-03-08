package com.aquaflow.service;

import com.aquaflow.config.WaterTariffProperties;
import com.aquaflow.dto.response.BillCalculation;
import com.aquaflow.entity.Meter;
import com.aquaflow.entity.WaterBill;
import com.aquaflow.exception.MeterNotFoundException;
import com.aquaflow.repository.MeterRepository;
import com.aquaflow.repository.WaterBillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaterBillingService {

    private final WaterTariffProperties tariff;
    private final MeterRepository meterRepo;
    private final WaterBillRepository billRepo;

    public BillCalculation calculateBill(long previousReading, long currentReading, String meterNumber, String tenantName) {
        long usageLitres = currentReading - previousReading;
        if (usageLitres < 0) throw new IllegalArgumentException("Current reading must be >= previous reading");

        double units = usageLitres / 1000.0;
        double remaining = units;
        double total = 0;
        List<BillCalculation.TierBreakdown> breakdown = new ArrayList<>();

        // Tier 1
        if (remaining > 0) {
            double t = Math.min(remaining, tariff.getTier1Limit());
            double sub = t * tariff.getTier1Rate();
            total += sub;
            breakdown.add(BillCalculation.TierBreakdown.builder()
                    .tier("0-" + tariff.getTier1Limit() + " units")
                    .units(BigDecimal.valueOf(t).setScale(2, RoundingMode.HALF_UP))
                    .rate(BigDecimal.valueOf(tariff.getTier1Rate()))
                    .subtotal(BigDecimal.valueOf(sub).setScale(0, RoundingMode.HALF_UP)).build());
            remaining -= t;
        }
        // Tier 2
        if (remaining > 0) {
            double t = Math.min(remaining, tariff.getTier2Limit() - tariff.getTier1Limit());
            double sub = t * tariff.getTier2Rate();
            total += sub;
            breakdown.add(BillCalculation.TierBreakdown.builder()
                    .tier((tariff.getTier1Limit() + 1) + "-" + tariff.getTier2Limit() + " units")
                    .units(BigDecimal.valueOf(t).setScale(2, RoundingMode.HALF_UP))
                    .rate(BigDecimal.valueOf(tariff.getTier2Rate()))
                    .subtotal(BigDecimal.valueOf(sub).setScale(0, RoundingMode.HALF_UP)).build());
            remaining -= t;
        }
        // Tier 3
        if (remaining > 0) {
            double t = Math.min(remaining, tariff.getTier3Limit() - tariff.getTier2Limit());
            double sub = t * tariff.getTier3Rate();
            total += sub;
            breakdown.add(BillCalculation.TierBreakdown.builder()
                    .tier((tariff.getTier2Limit() + 1) + "-" + tariff.getTier3Limit() + " units")
                    .units(BigDecimal.valueOf(t).setScale(2, RoundingMode.HALF_UP))
                    .rate(BigDecimal.valueOf(tariff.getTier3Rate()))
                    .subtotal(BigDecimal.valueOf(sub).setScale(0, RoundingMode.HALF_UP)).build());
            remaining -= t;
        }
        // Tier 4
        if (remaining > 0) {
            double sub = remaining * tariff.getTier4Rate();
            total += sub;
            breakdown.add(BillCalculation.TierBreakdown.builder()
                    .tier("50+ units")
                    .units(BigDecimal.valueOf(remaining).setScale(2, RoundingMode.HALF_UP))
                    .rate(BigDecimal.valueOf(tariff.getTier4Rate()))
                    .subtotal(BigDecimal.valueOf(sub).setScale(0, RoundingMode.HALF_UP)).build());
        }

        return BillCalculation.builder()
                .meterNumber(meterNumber).tenantName(tenantName)
                .previousReading(previousReading).currentReading(currentReading)
                .usageLitres(usageLitres)
                .unitsConsumed(BigDecimal.valueOf(units).setScale(2, RoundingMode.HALF_UP))
                .totalAmount(BigDecimal.valueOf(total).setScale(0, RoundingMode.HALF_UP))
                .breakdown(breakdown).build();
    }

    public Mono<BillCalculation> calculateBillForMeter(String meterNumber, Long currentReading) {
        return meterRepo.findByMeterNumber(meterNumber)
                .switchIfEmpty(Mono.error(new MeterNotFoundException(meterNumber)))
                .map(meter -> {
                    long prev = currentReading != null && currentReading > 0
                            ? meter.getPreviousReading()
                            : meter.getPreviousReading();
                    long curr = currentReading != null && currentReading > 0
                            ? currentReading
                            : meter.getCurrentReading();
                    return calculateBill(prev, curr, meter.getMeterNumber(), meter.getTenantName());
                });
    }

    public Mono<WaterBill> generateBill(String meterNumber) {
        return meterRepo.findByMeterNumber(meterNumber)
                .switchIfEmpty(Mono.error(new MeterNotFoundException(meterNumber)))
                .flatMap(meter -> {
                    BillCalculation calc = calculateBill(meter.getPreviousReading(), meter.getCurrentReading(),
                            meter.getMeterNumber(), meter.getTenantName());
                    String period = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));

                    WaterBill bill = WaterBill.builder()
                            .meterId(meter.getId()).meterNumber(meterNumber).billingPeriod(period)
                            .previousReading(meter.getPreviousReading()).currentReading(meter.getCurrentReading())
                            .usageLitres(calc.getUsageLitres())
                            .unitsConsumed(calc.getUnitsConsumed()).amount(calc.getTotalAmount())
                            .status("UNPAID").createdAt(LocalDateTime.now()).build();
                    return billRepo.save(bill);
                });
    }

    public Mono<WaterBill> markBillPaid(String meterNumber, String receipt) {
        return billRepo.findByMeterNumberAndStatus(meterNumber, "UNPAID")
                .next()
                .flatMap(bill -> {
                    bill.setStatus("PAID");
                    bill.setPaidAt(LocalDateTime.now());
                    bill.setMpesaReceipt(receipt);
                    return billRepo.save(bill);
                });
    }

    public Flux<WaterBill> getBillsByMeter(String meterNumber) {
        return billRepo.findByMeterNumber(meterNumber);
    }

    public Flux<WaterBill> getUnpaidBills() {
        return billRepo.findByStatus("UNPAID");
    }
}
