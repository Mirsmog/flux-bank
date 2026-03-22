package com.fluxbank.transaction.application.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class LedgerEntryDto {
    UUID id;
    UUID accountId;
    String entryType;
    BigDecimal amount;
    String currency;
    UUID correlationId;
    String description;
    Instant occurredAt;
}
