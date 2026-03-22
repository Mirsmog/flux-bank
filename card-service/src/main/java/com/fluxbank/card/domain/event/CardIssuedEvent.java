package com.fluxbank.card.domain.event;

import com.fluxbank.common.events.BaseEvent;
import lombok.Getter;

@Getter
public class CardIssuedEvent extends BaseEvent {
    private final String cardId;
    private final String userId;
    private final String accountId;
    private final String maskedPan;
    private final String lastFourDigits;
    private final String cardType;

    public CardIssuedEvent(String cardId, String userId, String accountId,
                           String maskedPan, String lastFourDigits, String cardType) {
        super("card.issued");
        this.cardId = cardId;
        this.userId = userId;
        this.accountId = accountId;
        this.maskedPan = maskedPan;
        this.lastFourDigits = lastFourDigits;
        this.cardType = cardType;
    }

    @Override
    public String getAggregateId() { return cardId; }
}
