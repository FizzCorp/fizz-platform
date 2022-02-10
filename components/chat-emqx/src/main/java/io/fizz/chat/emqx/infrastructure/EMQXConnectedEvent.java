package io.fizz.chat.emqx.infrastructure;

import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.DomainEventType;

import java.util.Date;
import java.util.Objects;

public class EMQXConnectedEvent implements AbstractDomainEvent {
    public static final DomainEventType TYPE = new DomainEventType("emqxConnected", EMQXConnectedEvent.class);

    private final long occurredOn;
    private final String clientId;
    private final String username;

    public EMQXConnectedEvent(final String aClientId, final String aUsername, final long aOccurredOn) {
        clientId = aClientId;
        username = aUsername;

        occurredOn = aOccurredOn;
    }

    public String getClientId() {
        return clientId;
    }
    public String getUsername() {
        return username;
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
        EMQXConnectedEvent that = (EMQXConnectedEvent) o;
        return occurredOn == that.occurredOn &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(occurredOn, clientId, username);
    }
}
