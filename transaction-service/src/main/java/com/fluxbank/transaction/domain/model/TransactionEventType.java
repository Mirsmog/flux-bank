package com.fluxbank.transaction.domain.model;

public enum TransactionEventType {
    DEPOSIT,          // money coming in from external source
    WITHDRAWAL,       // money going out to external destination
    TRANSFER_DEBIT,   // outgoing leg of an internal transfer
    TRANSFER_CREDIT,  // incoming leg of an internal transfer
    FEE,              // service fee deducted
    REVERSAL          // reversal of a previous transaction
}
