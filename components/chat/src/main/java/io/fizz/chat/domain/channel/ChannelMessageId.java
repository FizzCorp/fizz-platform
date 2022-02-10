package io.fizz.chat.domain.channel;

import io.fizz.common.Utils;

import java.util.Objects;

public class ChannelMessageId {
    private final long value;

    public ChannelMessageId(long aValue) {
        Utils.assertArgumentRange(aValue, 0, Long.MAX_VALUE, "invalid_message_id");

        this.value = aValue;
    }

    public long value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelMessageId that = (ChannelMessageId) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
