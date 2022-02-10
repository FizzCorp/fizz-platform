package io.fizz.chatcommon.domain.events;

import io.fizz.common.Utils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DomainEventType {
    private static final Map<String,Class<?>> registry = new ConcurrentHashMap<>();

    public static Class<?> toClass(final String aType) {
        return registry.get(aType);
    }

    private final String value;
    public DomainEventType(final String aValue, final Class<?> aClazz) {
        Utils.assertRequiredArgument(aValue, "invalid event type specified");
        Utils.assertArgumentNull(registry.get(aValue), "event type already registered");

        registry.put(aValue, aClazz);
        value = aValue;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainEventType that = (DomainEventType) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }
}
