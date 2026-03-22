package com.fluxbank.payment.infrastructure.client;

import com.fluxbank.common.dto.ApiResponse;
import com.fluxbank.payment.infrastructure.client.dto.RecordTransactionRequest;
import com.fluxbank.payment.infrastructure.client.dto.TransactionEventDto;
import com.fluxbank.payment.infrastructure.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "transaction-service", configuration = FeignConfig.class)
public interface TransactionServiceClient {

    @PostMapping("/api/v1/transactions/accounts/{accountId}/withdraw")
    ApiResponse<TransactionEventDto> withdraw(@PathVariable("accountId") UUID accountId,
                                              @RequestHeader("X-User-Id") String userId,
                                              @RequestBody RecordTransactionRequest request);

    @PostMapping("/api/v1/transactions/accounts/{accountId}/deposit")
    ApiResponse<TransactionEventDto> deposit(@PathVariable("accountId") UUID accountId,
                                             @RequestHeader("X-User-Id") String userId,
                                             @RequestBody RecordTransactionRequest request);
}
