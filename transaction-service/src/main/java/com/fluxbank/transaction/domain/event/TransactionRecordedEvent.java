package com.fluxbank.transaction.domain.event;

import com.fluxbank.common.events.BaseEvent;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Kafka domain event published after every successful transaction recording.
 * eventType = "transaction.recorded"
 */
@Getter
public class TransactionRecordedEvent extends BaseEvent {

    private final String correlationId;
    private final String accountId;
    private final String transactionType;
    private final BigDecimal amount;
    private final String currency;

    public TransactionRecordedEvent(String correlationId,
                                    String accountId,
                                    String transactionType,
                                    BigDecimal amount,
                                    String currency) {
        super("transaction.recorded");
        this.correlationId = correlationId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
    }

    @Override
    public String getAggregateId() {
        return correlationId;
    }
}
