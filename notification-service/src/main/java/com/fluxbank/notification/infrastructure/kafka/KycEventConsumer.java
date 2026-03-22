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
public class KycEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.KYC_EVENTS,
                   groupId = "notification-kyc-consumer",
                   containerFactory = "kafkaListenerContainerFactory")
    public void handleKycEvent(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String eventType = node.path("eventType").asText();

            if (!"kyc.status_changed".equals(eventType)) return;

            String userId = node.path("userId").asText();
            String newStatus = node.path("newStatus").asText();

            if (userId.isBlank()) return;

            if ("APPROVED".equals(newStatus)) {
                log.info("KYC approved event: userId={}", userId);
                notificationService.sendNotification(
                        UUID.fromString(userId),
                        "KYC Verification Approved",
                        "Your KYC verification was approved! You now have full access to FluxBank services.",
                        NotificationChannel.EMAIL
                );
            } else if ("REJECTED".equals(newStatus)) {
                String rejectionReason = node.path("rejectionReason").asText("No reason provided");
                log.info("KYC rejected event: userId={}", userId);
                notificationService.sendNotification(
                        UUID.fromString(userId),
                        "KYC Verification Rejected",
                        "Your KYC verification was rejected: " + rejectionReason,
                        NotificationChannel.EMAIL
                );
            }
        } catch (Exception e) {
            log.error("Failed to process KYC event: {}", e.getMessage(), e);
        }
    }
}
