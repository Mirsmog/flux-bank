package com.fluxbank.payment.infrastructure.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Subset of account-service AccountDto consumed by payment-service.
 */
public record AccountDetailsDto(
        UUID id,
        String accountNumber,
        String status,
        BigDecimal balance,
        String currency
) {}
