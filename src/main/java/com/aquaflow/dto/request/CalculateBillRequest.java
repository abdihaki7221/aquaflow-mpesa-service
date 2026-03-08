package com.aquaflow.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CalculateBillRequest {
    @NotBlank private String meterNumber;
    @Positive  private Long currentReading;
}
