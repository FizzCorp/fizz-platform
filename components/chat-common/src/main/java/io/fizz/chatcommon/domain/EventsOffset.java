package io.fizz.chatcommon.domain;

import java.util.Objects;

public class EventsOffset {
    private final long value;

    public EventsOffset() {
        this(0L);
    }

    public EventsOffset(long aValue) {
        if (aValue < 0) {
            throw new IllegalArgumentException("invalid_event_offset");
        }

        value = aValue;
    }

    public long value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventsOffset that = (EventsOffset) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
