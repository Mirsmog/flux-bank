package com.fluxbank.kyc.domain.event;

import com.fluxbank.common.events.BaseEvent;
import lombok.Getter;

@Getter
public class KycSubmittedEvent extends BaseEvent {
    private final String kycId;
    private final String userId;
    private final String documentType;

    public KycSubmittedEvent(String kycId, String userId, String documentType) {
        super("kyc.submitted");
        this.kycId = kycId;
        this.userId = userId;
        this.documentType = documentType;
    }

    @Override
    public String getAggregateId() { return kycId; }
}
