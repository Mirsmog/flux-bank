package com.fluxbank.kyc.application.dto;

import lombok.Builder;
import lombok.Value;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder
public class KycDto {
    UUID id;
    UUID userId;
    String status;
    String documentType;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    Instant submittedAt;
    Instant reviewedAt;
    String rejectionReason;
}
