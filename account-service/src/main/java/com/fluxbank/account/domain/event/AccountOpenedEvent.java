package com.fluxbank.account.domain.event;

import com.fluxbank.common.events.BaseEvent;
import lombok.Getter;

@Getter
public class AccountOpenedEvent extends BaseEvent {

    private static final String EVENT_TYPE = "account.opened";

    private final String accountId;
    private final String userId;
    private final String accountNumber;
    private final String accountType;
    private final String currency;

    public AccountOpenedEvent(String accountId, String userId, String accountNumber,
                              String accountType, String currency) {
        super(EVENT_TYPE);
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.currency = currency;
    }

    @Override
    public String getAggregateId() {
        return accountId;
    }
}
