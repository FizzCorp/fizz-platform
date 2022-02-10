package io.fizz.chat.pubsub.domain.subscriber;

import io.fizz.common.Utils;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

public class SubscriberId {
    public static int ID_LEN = 32;
    public static IllegalArgumentException ERROR_INVALID_VALUE = new IllegalArgumentException("invalid_subscriber_id");

    private final String value;

    public SubscriberId(final String aValue) {
        try {
            Utils.assertRequiredArgument(aValue, ERROR_INVALID_VALUE);

            final String trimmedValue = aValue.trim();
            int len = trimmedValue.getBytes("UTF-8").length;
            if (len != ID_LEN) {
                throw ERROR_INVALID_VALUE;
            }

            value = trimmedValue;
        }
        catch (UnsupportedEncodingException ex) {
            throw ERROR_INVALID_VALUE;
        }
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriberId that = (SubscriberId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }
}
