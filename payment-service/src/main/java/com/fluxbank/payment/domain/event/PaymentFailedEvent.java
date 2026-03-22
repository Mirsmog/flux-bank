package com.fluxbank.payment.domain.event;

import com.fluxbank.common.events.BaseEvent;
import lombok.Getter;

/** eventType = "payment.failed" */
@Getter
public class PaymentFailedEvent extends BaseEvent {

    private final String paymentId;
    private final String senderId;
    private final String failureReason;
    private final String status; // FAILED or COMPENSATED

    public PaymentFailedEvent(String paymentId,
                              String senderId,
                              String failureReason,
                              String status) {
        super("payment.failed");
        this.paymentId = paymentId;
        this.senderId = senderId;
        this.failureReason = failureReason;
        this.status = status;
    }

    @Override
    public String getAggregateId() {
        return paymentId;
    }
}
