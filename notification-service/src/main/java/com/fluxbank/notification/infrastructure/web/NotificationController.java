package com.fluxbank.notification.infrastructure.web;

import com.fluxbank.common.dto.ApiResponse;
import com.fluxbank.notification.application.dto.NotificationDto;
import com.fluxbank.notification.application.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getMyNotifications(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<NotificationDto> notifications = notificationService.getUserNotifications(
                UUID.fromString(userId), page, size);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }
}
