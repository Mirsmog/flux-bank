package com.fluxbank.notification.application.service.impl;

import com.fluxbank.notification.application.dto.NotificationDto;
import com.fluxbank.notification.application.mapper.NotificationMapper;
import com.fluxbank.notification.application.service.NotificationService;
import com.fluxbank.notification.domain.model.Notification;
import com.fluxbank.notification.domain.model.NotificationChannel;
import com.fluxbank.notification.domain.model.NotificationStatus;
import com.fluxbank.notification.domain.repository.NotificationRepository;
import com.fluxbank.notification.infrastructure.email.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailSender emailSender;

    @Override
    @Transactional
    public void sendNotification(UUID userId, String subject, String body, NotificationChannel channel) {
        Notification notification = Notification.builder()
                .userId(userId)
                .channel(channel)
                .subject(subject)
                .body(body)
                .status(NotificationStatus.PENDING)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification created: id={}, userId={}, channel={}", notification.getId(), userId, channel);

        try {
            if (channel == NotificationChannel.EMAIL) {
                emailSender.send(userId, subject, body);
            } else {
                log.info("Channel {} not yet implemented, logging only. userId={}", channel, userId);
            }
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            log.info("Notification sent: id={}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send notification id={}: {}", notification.getId(), e.getMessage(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage() != null
                    ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500))
                    : "Unknown error");
        }

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(UUID userId, int page, int size) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .stream()
                .map(notificationMapper::toDto)
                .toList();
    }
}
