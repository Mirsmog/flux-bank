package com.fluxbank.account.application.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class AccountSummaryDto {

    UUID id;
    String accountNumber;
    String type;
    String status;
    String name;

    /** Available balance: raw balance minus reserved balance. */
    BigDecimal balance;
    String currency;

    Instant createdAt;
}
