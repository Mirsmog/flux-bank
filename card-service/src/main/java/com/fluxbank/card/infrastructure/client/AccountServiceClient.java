package com.fluxbank.card.infrastructure.client;

import com.fluxbank.card.infrastructure.client.dto.AccountDetailsDto;
import com.fluxbank.card.infrastructure.config.FeignConfig;
import com.fluxbank.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.UUID;

@FeignClient(name = "account-service", configuration = FeignConfig.class)
public interface AccountServiceClient {

    @GetMapping("/api/v1/accounts/{id}")
    ApiResponse<AccountDetailsDto> getAccount(@PathVariable("id") UUID accountId,
                                              @RequestHeader("X-User-Id") String userId);

    @GetMapping("/api/v1/accounts/{id}/lookup")
    ApiResponse<AccountDetailsDto> lookupAccount(@PathVariable("id") UUID accountId,
                                                 @RequestHeader("X-User-Id") String userId);
}
