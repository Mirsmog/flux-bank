package com.fluxbank.transaction.domain.repository;

import com.fluxbank.transaction.domain.model.TransactionEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionEventRepository extends JpaRepository<TransactionEvent, UUID> {

    Page<TransactionEvent> findByAccountIdOrderByOccurredAtDesc(UUID accountId, Pageable pageable);

    List<TransactionEvent> findByCorrelationId(UUID correlationId);

    List<TransactionEvent> findByAccountIdAndOccurredAtBetweenOrderByOccurredAtAsc(
            UUID accountId, Instant from, Instant to);
}
