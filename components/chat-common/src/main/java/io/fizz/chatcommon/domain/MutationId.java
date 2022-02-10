package io.fizz.chatcommon.domain;

import java.util.Objects;

public class MutationId {
    private final int value;

    public MutationId() {
        this(0);
    }

    public MutationId(int aValue) {
        if (aValue < 0) {
            throw new IllegalArgumentException("invalid_mutation_id");
        }

        value = aValue;
    }

    public boolean isNull() {
        return value == 0;
    }

    public int value() {
        return value;
    }

    public int next() {
        return value + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MutationId that = (MutationId) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
