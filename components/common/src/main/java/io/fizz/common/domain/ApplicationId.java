package io.fizz.common.domain;

import io.fizz.common.Utils;

import java.util.Objects;

public class ApplicationId {
    public static final DomainErrorException ERROR_INVALID_APP_ID = new DomainErrorException(new DomainError("invalid_app_id"));
    private static final int MAX_ID_LEN = 64;

    private String value;

    public ApplicationId(String aValue) throws DomainErrorException {
        if (Objects.isNull(aValue) || Utils.isEmpty(aValue) || aValue.length() > MAX_ID_LEN) {
            throw ERROR_INVALID_APP_ID;
        }
        value = aValue;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationId that = (ApplicationId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
