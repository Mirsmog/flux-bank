package com.fluxbank.account.application.service;

import com.fluxbank.account.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates collision-free 16-digit account numbers.
 * Format: "4276" prefix + 12 random decimal digits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountNumberGenerator {

    private static final String PREFIX = "4276";
    private static final int SUFFIX_LENGTH = 12;
    private static final int MAX_ATTEMPTS = 10;

    private final SecureRandom secureRandom = new SecureRandom();
    private final AccountRepository accountRepository;

    public String generate() {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String candidate = PREFIX + randomDigits(SUFFIX_LENGTH);
            if (!accountRepository.existsByAccountNumber(candidate)) {
                log.debug("Generated account number on attempt {}", attempt);
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Unable to generate a unique account number after " + MAX_ATTEMPTS + " attempts");
    }

    private String randomDigits(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}
