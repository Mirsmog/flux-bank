package com.fluxbank.payment.application.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class PaymentSummaryDto {

    UUID id;
    UUID senderAccountId;
    UUID receiverAccountId;
    BigDecimal amount;
    String currency;
    String status;
    String type;
    Instant createdAt;
    Instant completedAt;
}
