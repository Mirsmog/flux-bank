package com.fluxbank.account.application.dto;

import com.fluxbank.account.domain.model.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(

        @NotNull(message = "Status is required")
        AccountStatus status
) {}
