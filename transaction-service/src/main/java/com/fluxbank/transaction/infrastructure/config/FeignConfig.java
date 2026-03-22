package com.fluxbank.transaction.infrastructure.config;

import com.fluxbank.common.exception.ErrorCode;
import com.fluxbank.common.exception.FluxBankException;
import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

// NOTE: intentionally NOT annotated with @Configuration — if it were, Spring's
// component scan would register these beans globally, applying them to ALL Feign
// clients. By omitting @Configuration, Feign creates its own child context and
// these beans only affect the account-service client.
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            int status = response.status();
            if (status == 404) {
                return new FluxBankException(ErrorCode.NOT_FOUND,
                        "Account not found or access denied");
            } else if (status >= 400 && status < 500) {
                return new FluxBankException(ErrorCode.VALIDATION_ERROR,
                        "Account service rejected request: HTTP " + status);
            } else {
                return new FluxBankException(ErrorCode.INTERNAL_ERROR,
                        "Account service error: HTTP " + status);
            }
        };
    }
}
