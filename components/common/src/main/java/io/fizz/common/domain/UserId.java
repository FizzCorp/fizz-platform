package io.fizz.common.domain;

import io.fizz.common.Utils;

import java.util.Objects;

public class UserId {
    public static final IllegalArgumentException ERROR_INVALID_USER_ID = new IllegalArgumentException("invalid_user_id");
    private static final int MAX_ID_LEN = 64;

    private String value;

    public UserId(final String aValue) {
        if (Objects.isNull(aValue) || Utils.isEmpty(aValue)) {
            throw ERROR_INVALID_USER_ID;
        }

        final String trimmed = aValue.trim();
        if (trimmed.length() > MAX_ID_LEN) {
            throw ERROR_INVALID_USER_ID;
        }

        value = trimmed;
    }

    public String value() {
        return value;
    }

    public String qualifiedValue(final ApplicationId aAppId) {
        if (Objects.isNull(aAppId)) {
            throw new IllegalArgumentException("invalid_app_id");
        }

        return aAppId.value() + ":" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
