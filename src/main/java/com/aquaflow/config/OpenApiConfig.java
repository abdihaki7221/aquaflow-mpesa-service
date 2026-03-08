package com.aquaflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI aquaFlowOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("AquaFlow Water Billing & M-Pesa API")
                .version("1.0.0")
                .description("APIs for water billing, meter management, M-Pesa C2B payments, and STK push")
                .contact(new Contact().name("AquaFlow").email("info@aquaflow.co.ke")));
    }
}
