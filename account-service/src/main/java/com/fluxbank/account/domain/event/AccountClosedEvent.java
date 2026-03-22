package com.fluxbank.account.domain.event;

import com.fluxbank.common.events.BaseEvent;
import lombok.Getter;

@Getter
public class AccountClosedEvent extends BaseEvent {

    private static final String EVENT_TYPE = "account.closed";

    private final String accountId;
    private final String userId;
    private final String accountNumber;

    public AccountClosedEvent(String accountId, String userId, String accountNumber) {
        super(EVENT_TYPE);
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
    }

    @Override
    public String getAggregateId() {
        return accountId;
    }
}
