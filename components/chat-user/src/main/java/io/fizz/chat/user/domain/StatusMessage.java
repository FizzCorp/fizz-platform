package io.fizz.chat.user.domain;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

public class StatusMessage {
    public static final IllegalArgumentException ERROR_STATUS_MESSAGE = new IllegalArgumentException("invalid_status_message");
    public static final int MAX_STATUS_MESSAGE_LEN = 128;

    private final String value;

    public StatusMessage(String aValue) {
        if (Objects.isNull(aValue)) {
            throw ERROR_STATUS_MESSAGE;
        }

        try {
            final String trimmed = aValue.trim();
            int len = trimmed.getBytes("UTF-8").length;
            if (len > MAX_STATUS_MESSAGE_LEN) {
                throw ERROR_STATUS_MESSAGE;
            }
            this.value = trimmed;
        } catch (UnsupportedEncodingException ex) {
            throw ERROR_STATUS_MESSAGE;
        }
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusMessage that = (StatusMessage) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
