package io.fizz.chat.emqx.infrastructure;

import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.DomainEventType;

import java.util.Date;
import java.util.Objects;

public class EMQXDisconnectedEvent implements AbstractDomainEvent {
    public static final DomainEventType TYPE = new DomainEventType("emqxDisconnected", EMQXDisconnectedEvent.class);

    private final long occurredOn;
    private final String clientId;

    public EMQXDisconnectedEvent(final String aClientId, final long aOccurredOn) {
        clientId = aClientId;

        occurredOn = aOccurredOn;
    }

    public String getClientId() {
        return clientId;
    }


    @Override
    public Date occurredOn() {
        return new Date(occurredOn);
    }

    @Override
    public DomainEventType type() {
        return TYPE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EMQXDisconnectedEvent that = (EMQXDisconnectedEvent) o;
        return occurredOn == that.occurredOn &&
                Objects.equals(clientId, that.clientId);

    }

    @Override
    public int hashCode() {
        return Objects.hash(occurredOn, clientId);
    }
}
