package com.aquaflow.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StkPushRequestDto {
    @NotBlank private String meterNumber;
    @NotBlank private String phone;
    @Positive  private BigDecimal amount;
    private String description;
}
