package com.aquaflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aquaFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AquaFlow M-Pesa Integration API")
                        .description("""
                                Safaricom Daraja API integration service for AquaFlow.
                                
                                Features:
                                - C2B Payment Callbacks (Validation & Confirmation)
                                - C2B Register URL
                                - B2B Payment Disbursement (auto 50% of C2B)
                                - Transaction Queries by Reference ID & Account Number
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("AquaFlow Team")
                                .email("dev@aquaflow.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.aquaflow.com").description("Production")
                ));
    }
}
