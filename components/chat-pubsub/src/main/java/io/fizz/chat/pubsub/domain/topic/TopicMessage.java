package io.fizz.chat.pubsub.domain.topic;

import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.Objects;

public class TopicMessage {
    private static final int MAX_TYPE_LEN = 32;
    private static final int MAX_FROM_LEN = 64;
    private static final IllegalArgumentException ERROR_INVALID_ID = new IllegalArgumentException("invalid_message_id");
    private static final IllegalArgumentException ERROR_INVALID_TYPE = new IllegalArgumentException("invalid_message_type");
    private static final IllegalArgumentException ERROR_INVALID_DATA = new IllegalArgumentException("invalid_message_data");
    private static final IllegalArgumentException ERROR_INVALID_FROM = new IllegalArgumentException("invalid_message_from");
    private static final IllegalArgumentException ERROR_INVALID_TIME = new IllegalArgumentException("invalid_message_time");

    private final long id;
    private final String type;
    protected final String data;
    private final UserId from;
    private final long occurredOn;

    public TopicMessage(final Long aId,
                        final String aType,
                        final String aFrom,
                        final String aData,
                        final Date aOccurredOn) throws IllegalArgumentException {
        if (Objects.isNull(aId) || aId < 0) {
            throw ERROR_INVALID_ID;
        }

        Utils.assertOptionalArgumentLength(aType, MAX_TYPE_LEN, ERROR_INVALID_TYPE);
        Utils.assertOptionalArgumentLength(aFrom, MAX_FROM_LEN, ERROR_INVALID_FROM);
        Utils.assertRequiredArgument(aOccurredOn, ERROR_INVALID_TIME);

        id = aId;
        type = aType;
        data = aData;
        from = new UserId(aFrom);
        occurredOn = aOccurredOn.getTime();
    }

    public long id() {
        return id;
    }

    public String type() {
        return type;
    }

    public String data() {
        return data;
    }

    public UserId from() {
        return from;
    }

    public long occurredOn() {
        return occurredOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicMessage message = (TopicMessage) o;
        return id == message.id &&
                occurredOn == message.occurredOn &&
                Objects.equals(type, message.type) &&
                Objects.equals(data, message.data) &&
                Objects.equals(from, message.from);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, type, data, from, occurredOn);
    }
}
