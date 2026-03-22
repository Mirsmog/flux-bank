package com.fluxbank.transaction.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable event-sourcing record. Once written, never updated.
 * This is the append-only event store for all financial changes.
 */
@Entity
@Table(name = "transaction_events")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The account this event applies to. */
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    /** For transfers: the counterpart account. */
    @Column(name = "counterpart_account_id")
    private UUID counterpartAccountId;

    /**
     * Unique business-level transaction ID grouping both legs of a transfer.
     * Both TRANSFER_DEBIT and TRANSFER_CREDIT share the same correlationId.
     */
    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private TransactionEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    /** Amount is always positive; the eventType conveys direction. */
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /** Running balance snapshot AFTER this event was applied. */
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(name = "description", length = 500)
    private String description;

    /** Reference for external transactions (payment gateway ref, etc.). */
    @Column(name = "reference_id", length = 100)
    private String referenceId;

    /** Metadata as JSON string (flexible key-value pairs). */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
