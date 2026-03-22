package com.fluxbank.notification.application.service;

import com.fluxbank.notification.application.dto.NotificationDto;
import com.fluxbank.notification.domain.model.NotificationChannel;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void sendNotification(UUID userId, String subject, String body, NotificationChannel channel);
    List<NotificationDto> getUserNotifications(UUID userId, int page, int size);
}
