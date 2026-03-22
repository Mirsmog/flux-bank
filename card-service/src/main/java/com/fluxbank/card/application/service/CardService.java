package com.fluxbank.card.application.service;

import com.fluxbank.card.application.dto.CardDto;
import com.fluxbank.card.application.dto.CardSummaryDto;
import com.fluxbank.card.application.dto.IssueCardRequest;
import java.util.List;
import java.util.UUID;

public interface CardService {
    CardDto issueCard(UUID userId, IssueCardRequest request);
    CardDto getCard(UUID cardId, UUID userId);
    List<CardSummaryDto> getUserCards(UUID userId);
    List<CardSummaryDto> getAccountCards(UUID accountId, UUID userId);
    CardDto blockCard(UUID cardId, UUID userId, String reason);
    CardDto unblockCard(UUID cardId, UUID userId);
    CardDto closeCard(UUID cardId, UUID userId);
}
