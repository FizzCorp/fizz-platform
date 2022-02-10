package io.fizz.analytics.common;

import java.io.Serializable;
import java.util.Objects;

public class Month implements Serializable {
    private int value;

    public Month(int value) {
        if (value < 1 || value > 12) {
            throw new IllegalArgumentException("A valid month should be specified.");
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

        Month month = (Month) o;

        return value == month.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
