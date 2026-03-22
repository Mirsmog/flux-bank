package com.fluxbank.payment.infrastructure.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Subset of transaction-service TransactionEventDto consumed by payment-service.
 */
public record TransactionEventDto(
        UUID id,
        UUID accountId,
        UUID correlationId,
        String eventType,
        BigDecimal amount,
        String currency
) {}
