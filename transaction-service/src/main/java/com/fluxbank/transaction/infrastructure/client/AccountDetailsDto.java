package com.fluxbank.transaction.infrastructure.client;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Subset of account-service AccountDto used by transaction-service.
 * Only fields needed for validation and balance snapshots are included.
 */
public record AccountDetailsDto(
        UUID id,
        String accountNumber,
        String status,
        BigDecimal balance,
        String currency
) {}
