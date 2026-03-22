package com.fluxbank.payment.infrastructure.client;

import com.fluxbank.common.dto.ApiResponse;
import com.fluxbank.payment.infrastructure.client.dto.AccountDetailsDto;
import com.fluxbank.payment.infrastructure.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "account-service", configuration = FeignConfig.class)
public interface AccountServiceClient {

    /** Get account details by ID with ownership validation (X-User-Id must own the account). */
    @GetMapping("/api/v1/accounts/{id}")
    ApiResponse<AccountDetailsDto> getAccount(@PathVariable("id") UUID accountId,
                                              @RequestHeader("X-User-Id") String userId);

    /** Look up any account by ID without ownership check — used to validate recipient accounts. */
    @GetMapping("/api/v1/accounts/{id}/lookup")
    ApiResponse<AccountDetailsDto> lookupAccount(@PathVariable("id") UUID accountId,
                                                 @RequestHeader("X-User-Id") String userId);
}
