package com.fluxbank.card.application.dto;

import lombok.Builder;
import lombok.Value;
import java.util.UUID;

@Value
@Builder
public class CardSummaryDto {
    UUID id;
    UUID accountId;
    String maskedPan;
    String lastFourDigits;
    String type;
    String status;
    short expiryMonth;
    short expiryYear;
}
