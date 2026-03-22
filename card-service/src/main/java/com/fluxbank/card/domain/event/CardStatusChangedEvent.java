package com.fluxbank.card.domain.event;

import com.fluxbank.common.events.BaseEvent;
import lombok.Getter;

@Getter
public class CardStatusChangedEvent extends BaseEvent {
    private final String cardId;
    private final String userId;
    private final String newStatus;
    private final String reason;

    public CardStatusChangedEvent(String cardId, String userId, String newStatus, String reason) {
        super("card.status_changed");
        this.cardId = cardId;
        this.userId = userId;
        this.newStatus = newStatus;
        this.reason = reason;
    }

    @Override
    public String getAggregateId() { return cardId; }
}
