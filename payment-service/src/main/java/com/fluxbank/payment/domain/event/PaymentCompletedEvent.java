package com.fluxbank.payment.domain.event;

import com.fluxbank.common.events.BaseEvent;
import lombok.Getter;

import java.math.BigDecimal;

/** eventType = "payment.completed" */
@Getter
public class PaymentCompletedEvent extends BaseEvent {

    private final String paymentId;
    private final String senderId;
    private final String senderAccountId;
    private final String receiverAccountId;
    private final BigDecimal amount;
    private final String currency;
    private final String debitTransactionId;
    private final String creditTransactionId;

    public PaymentCompletedEvent(String paymentId,
                                 String senderId,
                                 String senderAccountId,
                                 String receiverAccountId,
                                 BigDecimal amount,
                                 String currency,
                                 String debitTransactionId,
                                 String creditTransactionId) {
        super("payment.completed");
        this.paymentId = paymentId;
        this.senderId = senderId;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
        this.currency = currency;
        this.debitTransactionId = debitTransactionId;
        this.creditTransactionId = creditTransactionId;
    }

    @Override
    public String getAggregateId() {
        return paymentId;
    }
}
