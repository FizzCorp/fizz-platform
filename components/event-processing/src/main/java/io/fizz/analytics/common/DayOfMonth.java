package io.fizz.analytics.common;

import java.io.Serializable;
import java.util.Objects;

public class DayOfMonth implements Serializable {
    private int value;

    public DayOfMonth(int value) {
        if (value < 1 || value > 31) {
            throw new IllegalArgumentException("A valid day of the month should be specified.");
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DayOfMonth that = (DayOfMonth) o;

        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
