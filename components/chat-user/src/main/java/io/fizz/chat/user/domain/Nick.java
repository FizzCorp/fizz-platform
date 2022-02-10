package io.fizz.chat.user.domain;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

public class Nick {
    public static final IllegalArgumentException ERROR_NICK = new IllegalArgumentException("invalid_nick");
    public static final int MAX_NICK_LEN = 64;

    private final String value;

    public Nick(String aValue) {
        if (Objects.isNull(aValue)) {
            throw ERROR_NICK;
        }

        try {
            final String trimmed = aValue.trim();
            int len = trimmed.getBytes("UTF-8").length;
            if (len > MAX_NICK_LEN) {
                throw ERROR_NICK;
            }
            this.value = trimmed;
        } catch (UnsupportedEncodingException ex) {
            throw ERROR_NICK;
        }
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nick that = (Nick) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
