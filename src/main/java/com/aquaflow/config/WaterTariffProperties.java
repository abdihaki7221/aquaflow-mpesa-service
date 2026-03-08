package com.aquaflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aquaflow.water-tariff")
public class WaterTariffProperties {
    private int tier1Limit = 6;
    private double tier1Rate = 45;
    private int tier2Limit = 20;
    private double tier2Rate = 55;
    private int tier3Limit = 50;
    private double tier3Rate = 70;
    private double tier4Rate = 80;
}
