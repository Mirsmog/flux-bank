package com.fluxbank.transaction.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transaction Service API")
                        .description("CQRS + Event Sourcing transaction service with double-entry ledger for flux-bank")
                        .version("1.0.0"));
    }
}
