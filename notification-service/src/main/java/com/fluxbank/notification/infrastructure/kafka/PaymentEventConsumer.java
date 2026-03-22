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
public class PaymentEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS,
                   groupId = "notification-payment-consumer",
                   containerFactory = "kafkaListenerContainerFactory")
    public void handlePaymentEvent(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String eventType = node.path("eventType").asText();
            String senderId = node.path("senderId").asText();

            if (senderId.isBlank()) return;
            UUID userId = UUID.fromString(senderId);

            if ("payment.completed".equals(eventType)) {
                String amount = node.path("amount").asText("?");
                String currency = node.path("currency").asText("?");
                log.info("Payment completed event: userId={}", userId);
                notificationService.sendNotification(
                        userId,
                        "Payment Completed",
                        "Your payment of " + amount + " " + currency + " was completed successfully.",
                        NotificationChannel.EMAIL
                );
            } else if ("payment.failed".equals(eventType)) {
                String failureReason = node.path("failureReason").asText("Unknown reason");
                log.info("Payment failed event: userId={}", userId);
                notificationService.sendNotification(
                        userId,
                        "Payment Failed",
                        "Your payment failed: " + failureReason,
                        NotificationChannel.EMAIL
                );
            }
        } catch (Exception e) {
            log.error("Failed to process payment event: {}", e.getMessage(), e);
        }
    }
}
