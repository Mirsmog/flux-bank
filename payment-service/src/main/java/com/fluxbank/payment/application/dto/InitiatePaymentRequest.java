package com.fluxbank.payment.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record InitiatePaymentRequest(
        @NotNull UUID senderAccountId,
        @NotNull UUID receiverAccountId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank @Size(max = 3) String currency,
        @Size(max = 500) String description
) {}
