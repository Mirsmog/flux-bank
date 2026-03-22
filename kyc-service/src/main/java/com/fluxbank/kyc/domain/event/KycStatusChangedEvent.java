package com.fluxbank.kyc.domain.event;

import com.fluxbank.common.events.BaseEvent;
import lombok.Getter;

@Getter
public class KycStatusChangedEvent extends BaseEvent {
    private final String kycId;
    private final String userId;
    private final String newStatus;
    private final String rejectionReason;

    public KycStatusChangedEvent(String kycId, String userId, String newStatus, String rejectionReason) {
        super("kyc.status_changed");
        this.kycId = kycId;
        this.userId = userId;
        this.newStatus = newStatus;
        this.rejectionReason = rejectionReason;
    }

    @Override
    public String getAggregateId() { return kycId; }
}
