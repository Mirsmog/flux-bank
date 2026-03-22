package com.fluxbank.card.infrastructure.config;

import com.fluxbank.common.exception.ErrorCode;
import com.fluxbank.common.exception.FluxBankException;
import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

// NOTE: intentionally NOT annotated with @Configuration — if it were, Spring's
// component scan would register these beans globally, applying them to ALL Feign
// clients. By omitting @Configuration, Feign creates its own child context and
// these beans only affect the specific client that references this class.
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
                        "Remote resource not found: " + methodKey);
            } else if (status >= 400 && status < 500) {
                return new FluxBankException(ErrorCode.VALIDATION_ERROR,
                        "Remote service rejected request: HTTP " + status + " [" + methodKey + "]");
            } else {
                return new FluxBankException(ErrorCode.INTERNAL_ERROR,
                        "Remote service error: HTTP " + status + " [" + methodKey + "]");
            }
        };
    }
}
