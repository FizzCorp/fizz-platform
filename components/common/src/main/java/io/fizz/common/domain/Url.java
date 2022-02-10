package io.fizz.common.domain;

import io.fizz.common.Utils;

import java.util.Objects;

public class Url {
    private final String URL_REGEX = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)";
    private final String value;

    public Url(final String aValue) {
        final String trimmed = Objects.isNull(aValue) ? null : aValue.trim();
        Utils.assertRequiedArgumentMatches(trimmed, URL_REGEX, "invalid_url");

        value = trimmed;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Url that = (Url) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
