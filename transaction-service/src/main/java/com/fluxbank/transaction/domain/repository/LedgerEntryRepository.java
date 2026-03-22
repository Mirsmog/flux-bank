package com.fluxbank.transaction.domain.repository;

import com.fluxbank.transaction.domain.model.LedgerEntry;
import com.fluxbank.transaction.domain.model.LedgerEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByAccountIdAndOccurredAtBetweenOrderByOccurredAtAsc(
            UUID accountId, Instant from, Instant to);

    /**
     * Computes the net balance for an account from all ledger entries before the given instant.
     * Credits are positive, debits are negative.
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN e.entryType = :credit THEN e.amount ELSE -e.amount END), 0) " +
           "FROM LedgerEntry e WHERE e.accountId = :accountId AND e.occurredAt < :before")
    BigDecimal computeBalanceBefore(@Param("accountId") UUID accountId,
                                    @Param("before") Instant before,
                                    @Param("credit") LedgerEntryType credit);
}
