package com.fluxbank.account.application.service;

import com.fluxbank.account.application.dto.AccountDto;
import com.fluxbank.account.application.dto.AccountSummaryDto;
import com.fluxbank.account.application.dto.CreateAccountRequest;
import com.fluxbank.account.application.dto.UpdateAccountStatusRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountDto openAccount(UUID userId, CreateAccountRequest request);

    List<AccountSummaryDto> getUserAccounts(UUID userId);

    AccountDto getAccount(UUID accountId, UUID userId);

    AccountDto getAccountByNumber(String accountNumber, UUID userId);

    AccountDto updateStatus(UUID accountId, UUID userId, UpdateAccountStatusRequest request);

    void closeAccount(UUID accountId, UUID userId);

    /**
     * Look up an account by ID without ownership validation.
     * Intended for internal service-to-service calls (e.g., transaction-service
     * validating the recipient in a transfer).
     */
    AccountDto getAccountById(UUID accountId);

    /**
     * Apply a balance delta to an account.
     * Positive delta = credit (increase). Negative delta = debit (decrease).
     * Called by transaction-service to keep account balances in sync after recording events.
     */
    AccountDto applyBalanceDelta(UUID accountId, BigDecimal delta);
}
