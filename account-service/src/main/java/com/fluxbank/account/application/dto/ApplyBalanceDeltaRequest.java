package com.fluxbank.account.application.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Internal request to apply a balance delta to an account.
 * Positive delta = credit (increase). Negative delta = debit (decrease).
 * Used by transaction-service via Feign to keep account balances in sync.
 */
public record ApplyBalanceDeltaRequest(
        @NotNull BigDecimal delta
) {}
