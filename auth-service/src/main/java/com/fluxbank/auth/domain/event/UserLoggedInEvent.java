package com.fluxbank.auth.domain.event;

import com.fluxbank.common.events.BaseEvent;
import lombok.Getter;

@Getter
public class UserLoggedInEvent extends BaseEvent {

    private static final String EVENT_TYPE = "auth.user.logged_in";

    private final String userId;
    private final String email;

    public UserLoggedInEvent(String userId, String email) {
        super(EVENT_TYPE);
        this.userId = userId;
        this.email = email;
    }

    @Override
    public String getAggregateId() {
        return userId;
    }
}
