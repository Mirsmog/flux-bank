package com.fluxbank.kyc.application.dto;

import com.fluxbank.kyc.domain.model.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SubmitKycRequest(
        @NotNull DocumentType documentType,
        @NotBlank @Size(max = 50) String documentNumber,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull @Past LocalDate dateOfBirth
) {}
