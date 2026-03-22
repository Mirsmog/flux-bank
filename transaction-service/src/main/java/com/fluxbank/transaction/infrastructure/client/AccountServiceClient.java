package com.fluxbank.transaction.infrastructure.client;

import com.fluxbank.common.dto.ApiResponse;
import com.fluxbank.transaction.infrastructure.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "account-service", configuration = FeignConfig.class)
public interface AccountServiceClient {

    /**
     * Get account details by ID with ownership validation.
     * Passes X-User-Id so account-service validates the account belongs to that user.
     */
    @GetMapping("/api/v1/accounts/{id}")
    ApiResponse<AccountDetailsDto> getAccount(@PathVariable("id") UUID accountId,
                                              @RequestHeader("X-User-Id") String userId);

    /**
     * Look up any account by ID without ownership check.
     * Used for validating the recipient account in transfer operations.
     * Still requires authentication (X-User-Id) but does not enforce ownership.
     */
    @GetMapping("/api/v1/accounts/{id}/lookup")
    ApiResponse<AccountDetailsDto> lookupAccount(@PathVariable("id") UUID accountId,
                                                 @RequestHeader("X-User-Id") String userId);

    /**
     * Apply a balance delta to an account (internal use only).
     * Positive delta = credit; negative delta = debit.
     * Called after recording each transaction event to keep account balances in sync.
     */
    @PutMapping("/api/v1/accounts/{id}/balance")
    ApiResponse<AccountDetailsDto> applyBalanceDelta(@PathVariable("id") UUID accountId,
                                                     @RequestHeader("X-User-Id") String userId,
                                                     @RequestBody Map<String, BigDecimal> body);
}
