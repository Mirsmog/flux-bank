package com.fluxbank.common.events;

import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

/**
 * Abstract base class for all domain events in flux-bank.
 *
 * <p>Subclasses must supply an {@code eventType} string that uniquely identifies
 * the event kind (e.g., {@code "auth.user.registered"}).
 * The {@code version} field supports schema evolution of event payloads.
 */
@Getter
@ToString
public abstract class BaseEvent implements DomainEvent {

    private final String eventId;
    private final String eventType;
    private final Instant occurredAt;
    private final int version;

    protected BaseEvent(String eventType) {
        this(eventType, 1);
    }

    protected BaseEvent(String eventType, int version) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.occurredAt = Instant.now();
        this.version = version;
    }

    /**
     * Returns the aggregate / entity ID associated with this event.
     * Implementing classes should return the ID of the entity that changed.
     */
    public abstract String getAggregateId();
}
