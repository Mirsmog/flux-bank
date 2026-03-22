package com.fluxbank.card.application.dto;

import com.fluxbank.card.domain.model.CardType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record IssueCardRequest(
        @NotNull UUID accountId,
        @NotNull CardType type,
        @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 digits") String pin
) {}
