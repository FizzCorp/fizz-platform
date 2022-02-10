package io.fizz.chataccess.domain.role;

import io.fizz.common.Utils;

import java.util.Objects;

public class RoleName {
    private final String value;

    public RoleName(final String aValue) {
        final String trimmedValue = Objects.isNull(aValue) ? null : aValue.trim();

        Utils.assertRequiredArgumentLength(trimmedValue, 32, "invalid_role_name");
        Utils.assertRequiedArgumentMatches(trimmedValue, "[A-Za-z0-9_-]+", "invalid_role_name");

        value = trimmedValue;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleName roleName = (RoleName) o;
        return Objects.equals(value, roleName.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }
}
