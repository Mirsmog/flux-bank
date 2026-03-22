package com.fluxbank.transaction.application.service;

import com.fluxbank.transaction.application.dto.*;

import java.time.Instant;
import java.util.UUID;

public interface TransactionService {

    /** Record a DEPOSIT — money coming in to accountId. */
    TransactionEventDto deposit(UUID accountId, UUID userId, RecordTransactionRequest request);

    /** Record a WITHDRAWAL — money going out from accountId. */
    TransactionEventDto withdraw(UUID accountId, UUID userId, RecordTransactionRequest request);

    /**
     * Record an internal TRANSFER — debit fromAccountId, credit toAccountId.
     * Returns the DEBIT leg event; correlationId ties both legs together.
     */
    TransactionEventDto transfer(UUID fromAccountId, UUID toAccountId, UUID userId,
                                 RecordTransactionRequest request);

    /** Get paginated transaction history for an account. */
    TransactionHistoryResponse getAccountHistory(UUID accountId, UUID userId, int page, int size);

    /** Get a single transaction event by ID. */
    TransactionEventDto getTransaction(UUID transactionId, UUID userId);

    /** Get double-entry ledger entries for an account within a date range. */
    AccountStatementDto getAccountStatement(UUID accountId, UUID userId, Instant from, Instant to);
}
