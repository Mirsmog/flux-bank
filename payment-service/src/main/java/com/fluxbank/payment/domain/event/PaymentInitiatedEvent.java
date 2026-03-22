package com.fluxbank.payment.domain.event;

import com.fluxbank.common.events.BaseEvent;
import lombok.Getter;

import java.math.BigDecimal;

/** eventType = "payment.initiated" */
@Getter
public class PaymentInitiatedEvent extends BaseEvent {

    private final String paymentId;
    private final String senderId;
    private final String senderAccountId;
    private final String receiverAccountId;
    private final BigDecimal amount;
    private final String currency;
    private final String paymentType;

    public PaymentInitiatedEvent(String paymentId,
                                 String senderId,
                                 String senderAccountId,
                                 String receiverAccountId,
                                 BigDecimal amount,
                                 String currency,
                                 String paymentType) {
        super("payment.initiated");
        this.paymentId = paymentId;
        this.senderId = senderId;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
        this.currency = currency;
        this.paymentType = paymentType;
    }

    @Override
    public String getAggregateId() {
        return paymentId;
    }
}
