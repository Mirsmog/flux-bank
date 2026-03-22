package com.fluxbank.account.application.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class AccountDto {

    UUID id;
    String accountNumber;
    UUID userId;
    String type;
    String status;
    String name;

    /** Available balance: raw balance minus reserved balance. */
    BigDecimal balance;
    BigDecimal reservedBalance;
    String currency;

    Instant createdAt;
    Instant updatedAt;
    Instant closedAt;
}
