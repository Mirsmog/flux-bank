package com.fluxbank.card.infrastructure.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountDetailsDto(
        UUID id,
        String accountNumber,
        String status,
        BigDecimal balance,
        String currency
) {}
