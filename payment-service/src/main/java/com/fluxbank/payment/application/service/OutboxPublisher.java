package com.fluxbank.payment.application.service;

import com.fluxbank.payment.domain.model.OutboxEvent;
import com.fluxbank.payment.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    // Uses KafkaTemplate<String, String> — payload is already a serialized JSON string.
    // Using a JsonSerializer here would double-encode the string into a JSON string literal.
    private final KafkaTemplate<String, String> stringKafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> unpublished =
                outboxEventRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();
        if (unpublished.isEmpty()) return;

        log.debug("Publishing {} outbox events", unpublished.size());

        for (OutboxEvent event : unpublished) {
            try {
                // Block until Kafka broker acknowledges receipt (acks=all).
                // Only mark as published after confirmed delivery so the event
                // is retried on the next scheduler cycle if Kafka is unavailable.
                stringKafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload()).get();
                event.markPublished();
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Outbox publish error for event id={}: {}", event.getId(), e.getMessage());
                // Event remains published=false and will be retried on the next cycle.
            }
        }
    }
}
