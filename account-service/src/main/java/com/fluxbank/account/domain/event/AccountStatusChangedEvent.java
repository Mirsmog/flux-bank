package com.fluxbank.account.domain.event;

import com.fluxbank.common.events.BaseEvent;
import lombok.Getter;

@Getter
public class AccountStatusChangedEvent extends BaseEvent {

    private static final String EVENT_TYPE = "account.status_changed";

    private final String accountId;
    private final String userId;
    private final String oldStatus;
    private final String newStatus;

    public AccountStatusChangedEvent(String accountId, String userId,
                                     String oldStatus, String newStatus) {
        super(EVENT_TYPE);
        this.accountId = accountId;
        this.userId = userId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    @Override
    public String getAggregateId() {
        return accountId;
    }
}
