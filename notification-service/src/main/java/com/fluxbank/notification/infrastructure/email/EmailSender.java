package com.fluxbank.notification.infrastructure.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {

    @Value("${notification.email.mock:true}")
    private boolean mockMode;

    public void send(UUID userId, String subject, String body) {
        if (mockMode) {
            log.info("📧 [MOCK EMAIL] To: userId={}, Subject={}, Body={}", userId, subject, body);
            return;
        }
        // Real SMTP sending would go here (JavaMailSender injection)
        log.info("📧 [EMAIL] To: userId={}, Subject={}", userId, subject);
    }
}
