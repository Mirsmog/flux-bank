package com.fluxbank.account.application.dto;

import com.fluxbank.account.domain.model.AccountType;
import com.fluxbank.account.domain.model.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(

        @NotNull(message = "Account type is required")
        AccountType type,

        @NotNull(message = "Currency is required")
        Currency currency,

        @NotBlank(message = "Account name is required")
        @Size(max = 100, message = "Account name must not exceed 100 characters")
        String name
) {}
