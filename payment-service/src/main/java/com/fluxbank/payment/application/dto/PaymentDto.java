package com.fluxbank.payment.application.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class PaymentDto {

    UUID id;
    String idempotencyKey;
    UUID senderId;
    UUID senderAccountId;
    UUID receiverAccountId;
    BigDecimal amount;
    String currency;
    String type;
    String status;
    String description;
    UUID debitTransactionId;
    UUID creditTransactionId;
    UUID compensationTransactionId;
    String failureReason;
    Instant createdAt;
    Instant updatedAt;
    Instant completedAt;
}
