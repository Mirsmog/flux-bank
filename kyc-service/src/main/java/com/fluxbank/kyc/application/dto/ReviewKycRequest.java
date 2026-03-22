package com.fluxbank.kyc.application.dto;

import jakarta.validation.constraints.Size;

public record ReviewKycRequest(
        boolean approved,
        @Size(max = 500) String rejectionReason
) {}
