package com.fluxbank.card.application.service.impl;

import com.fluxbank.card.application.dto.CardDto;
import com.fluxbank.card.application.dto.CardSummaryDto;
import com.fluxbank.card.application.dto.IssueCardRequest;
import com.fluxbank.card.application.mapper.CardMapper;
import com.fluxbank.card.application.service.CardService;
import com.fluxbank.card.domain.event.CardIssuedEvent;
import com.fluxbank.card.domain.event.CardStatusChangedEvent;
import com.fluxbank.card.domain.model.Card;
import com.fluxbank.card.domain.model.CardStatus;
import com.fluxbank.card.domain.repository.CardRepository;
import com.fluxbank.card.infrastructure.client.AccountServiceClient;
import com.fluxbank.card.infrastructure.client.dto.AccountDetailsDto;
import com.fluxbank.common.constants.KafkaTopics;
import com.fluxbank.common.exception.ErrorCode;
import com.fluxbank.common.exception.FluxBankException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final AccountServiceClient accountServiceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public CardDto issueCard(UUID userId, IssueCardRequest request) {
        AccountDetailsDto account = accountServiceClient
                .getAccount(request.accountId(), userId.toString())
                .getData();

        if (account == null) {
            throw new FluxBankException(ErrorCode.NOT_FOUND, "Account not found: " + request.accountId());
        }
        if (!"ACTIVE".equals(account.status())) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Account is not active. Current status: " + account.status());
        }

        String lastFour = generateLastFour();
        String maskedPan = "****-****-****-" + lastFour;
        String cvv = generateCvv();
        String cvvHash = passwordEncoder.encode(cvv);
        short expiryMonth = (short) (secureRandom.nextInt(12) + 1);
        short expiryYear = (short) (Year.now().getValue() + 3);

        Card card = Card.builder()
                .accountId(request.accountId())
                .userId(userId)
                .type(request.type())
                .status(CardStatus.ACTIVE)
                .maskedPan(maskedPan)
                .lastFourDigits(lastFour)
                .cvvHash(cvvHash)
                .expiryMonth(expiryMonth)
                .expiryYear(expiryYear)
                .build();

        card = cardRepository.save(card);
        log.info("Card issued: cardId={}, userId={}, accountId={}", card.getId(), userId, request.accountId());

        CardIssuedEvent event = new CardIssuedEvent(
                card.getId().toString(),
                userId.toString(),
                request.accountId().toString(),
                maskedPan,
                lastFour,
                request.type().name()
        );
        kafkaTemplate.send(KafkaTopics.CARD_EVENTS, card.getId().toString(), event);

        return cardMapper.toDto(card);
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto getCard(UUID cardId, UUID userId) {
        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND,
                        "Card not found: " + cardId));
        return cardMapper.toDto(card);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardSummaryDto> getUserCards(UUID userId) {
        return cardMapper.toSummaryDtoList(cardRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardSummaryDto> getAccountCards(UUID accountId, UUID userId) {
        return cardMapper.toSummaryDtoList(
                cardRepository.findByAccountIdAndUserIdOrderByCreatedAtDesc(accountId, userId));
    }

    @Override
    @Transactional
    public CardDto blockCard(UUID cardId, UUID userId, String reason) {
        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND, "Card not found: " + cardId));

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Only ACTIVE cards can be blocked. Current status: " + card.getStatus());
        }

        card.setStatus(CardStatus.BLOCKED);
        card = cardRepository.save(card);
        log.info("Card blocked: cardId={}, userId={}, reason={}", cardId, userId, reason);

        publishStatusChanged(card, reason);
        return cardMapper.toDto(card);
    }

    @Override
    @Transactional
    public CardDto unblockCard(UUID cardId, UUID userId) {
        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND, "Card not found: " + cardId));

        if (card.getStatus() != CardStatus.BLOCKED) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR,
                    "Only BLOCKED cards can be unblocked. Current status: " + card.getStatus());
        }

        card.setStatus(CardStatus.ACTIVE);
        card = cardRepository.save(card);
        log.info("Card unblocked: cardId={}, userId={}", cardId, userId);

        publishStatusChanged(card, null);
        return cardMapper.toDto(card);
    }

    @Override
    @Transactional
    public CardDto closeCard(UUID cardId, UUID userId) {
        Card card = cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new FluxBankException(ErrorCode.NOT_FOUND, "Card not found: " + cardId));

        if (card.getStatus() == CardStatus.CLOSED) {
            throw new FluxBankException(ErrorCode.VALIDATION_ERROR, "Card is already closed.");
        }

        card.setStatus(CardStatus.CLOSED);
        card = cardRepository.save(card);
        log.info("Card closed: cardId={}, userId={}", cardId, userId);

        publishStatusChanged(card, "Card closed by user");
        return cardMapper.toDto(card);
    }

    private void publishStatusChanged(Card card, String reason) {
        CardStatusChangedEvent event = new CardStatusChangedEvent(
                card.getId().toString(),
                card.getUserId().toString(),
                card.getStatus().name(),
                reason
        );
        kafkaTemplate.send(KafkaTopics.CARD_EVENTS, card.getId().toString(), event);
    }

    private String generateLastFour() {
        int num = secureRandom.nextInt(10000);
        return String.format("%04d", num);
    }

    private String generateCvv() {
        int num = secureRandom.nextInt(1000);
        return String.format("%03d", num);
    }
}
