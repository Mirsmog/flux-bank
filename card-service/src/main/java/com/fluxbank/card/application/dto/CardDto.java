package com.fluxbank.card.application.dto;

import lombok.Builder;
import lombok.Value;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class CardDto {
    UUID id;
    UUID accountId;
    UUID userId;
    String maskedPan;
    String lastFourDigits;
    String type;
    String status;
    short expiryMonth;
    short expiryYear;
    Instant createdAt;
}
