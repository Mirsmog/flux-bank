package com.fluxbank.notification.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluxbank.common.constants.KafkaTopics;
import com.fluxbank.notification.application.service.NotificationService;
import com.fluxbank.notification.domain.model.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.AUTH_EVENTS,
                   groupId = "notification-auth-consumer",
                   containerFactory = "kafkaListenerContainerFactory")
    public void handleAuthEvent(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String eventType = node.path("eventType").asText();
            String aggregateId = node.path("aggregateId").asText();

            if ("auth.user.registered".equals(eventType) && !aggregateId.isBlank()) {
                log.info("Auth event received: type={}, userId={}", eventType, aggregateId);
                notificationService.sendNotification(
                        UUID.fromString(aggregateId),
                        "Welcome to FluxBank!",
                        "Thank you for registering with FluxBank. Your account is now active.",
                        NotificationChannel.EMAIL
                );
            }
        } catch (Exception e) {
            log.error("Failed to process auth event: {}", e.getMessage(), e);
        }
    }
}
