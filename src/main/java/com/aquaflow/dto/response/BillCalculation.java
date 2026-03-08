package com.aquaflow.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BillCalculation {
    private String meterNumber;
    private String tenantName;
    private Long previousReading;
    private Long currentReading;
    private Long usageLitres;
    private BigDecimal unitsConsumed;
    private BigDecimal totalAmount;
    private List<TierBreakdown> breakdown;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TierBreakdown {
        private String tier;
        private BigDecimal units;
        private BigDecimal rate;
        private BigDecimal subtotal;
    }
}
