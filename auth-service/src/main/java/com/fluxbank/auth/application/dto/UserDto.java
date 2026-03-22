package com.fluxbank.auth.application.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class UserDto {
    UUID id;
    String email;
    String firstName;
    String lastName;
    String role;
    String status;
    Instant createdAt;
}
