package com.fluxbank.transaction.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable double-entry ledger record.
 * Every financial change produces at least one LedgerEntry (DEBIT or CREDIT).
 * The sum of all entries across all accounts must always be zero.
 */
@Entity
@Table(name = "ledger_entries")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** FK to transaction_events. */
    @Column(name = "transaction_event_id", nullable = false)
    private UUID transactionEventId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private LedgerEntryType entryType;

    /** Always positive; entryType conveys direction. */
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
