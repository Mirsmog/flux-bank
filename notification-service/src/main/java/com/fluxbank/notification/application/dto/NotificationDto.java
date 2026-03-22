package com.fluxbank.notification.application.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class NotificationDto {
    UUID id;
    UUID userId;
    String channel;
    String subject;
    String body;
    String status;
    int retryCount;
    Instant createdAt;
    Instant sentAt;
}
