package io.fizz.analytics.common;

import java.io.Serializable;
import java.util.Objects;

public class Year implements Serializable {
    private int value;

    public Year(int value) {
        if (value < 1970) {
            throw new IllegalArgumentException("Can not specify year before 1970");
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

        Year year = (Year) o;

        return value == year.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
