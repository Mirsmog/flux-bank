package com.fluxbank.auth.domain.event;

import com.fluxbank.common.events.BaseEvent;
import lombok.Getter;

@Getter
public class UserRegisteredEvent extends BaseEvent {

    private static final String EVENT_TYPE = "auth.user.registered";

    private final String userId;
    private final String email;
    private final String firstName;
    private final String lastName;

    public UserRegisteredEvent(String userId, String email, String firstName, String lastName) {
        super(EVENT_TYPE);
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String getAggregateId() {
        return userId;
    }
}
