package io.fizz.chatcommon.infrastructure;

import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.DomainEventType;

import java.util.Date;
import java.util.Objects;

public class SampleEvent implements AbstractDomainEvent {
    public static final DomainEventType TYPE = new DomainEventType("sampleEvent", SampleEvent.class);

    private final long occurredOn;
    private final String data;

    public SampleEvent(final String aData, final long aOccurredOn) {
        data = aData;
        occurredOn = aOccurredOn;
    }

    public String getData() {
        return data;
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
        SampleEvent that = (SampleEvent) o;
        return occurredOn == that.occurredOn &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {

        return Objects.hash(occurredOn, data);
    }
}
