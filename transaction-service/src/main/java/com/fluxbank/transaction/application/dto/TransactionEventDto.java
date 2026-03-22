package com.fluxbank.transaction.application.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class TransactionEventDto {
    UUID id;
    UUID accountId;
    UUID counterpartAccountId;
    UUID correlationId;
    String eventType;
    String status;
    BigDecimal amount;
    String currency;
    BigDecimal balanceAfter;
    String description;
    String referenceId;
    Instant occurredAt;
}
