package com.fluxbank.transaction.application.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class AccountStatementDto {
    UUID accountId;
    String accountNumber;
    Instant from;
    Instant to;
    BigDecimal openingBalance;
    BigDecimal closingBalance;
    String currency;
    List<LedgerEntryDto> entries;
}
