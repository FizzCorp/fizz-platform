package io.fizz.common.domain;

import java.util.HashMap;
import java.util.Map;

public enum EventType {
    SESSION_STARTED(100),
    SESSION_ENDED(101),
    NEW_USER_CREATED(102),
    PROFILE_UPDATED(103),
    PRODUCT_PURCHASED(200),
    TEXT_MESSAGE_SENT(300),
    TEXT_TRANSLATED(301),
    INVALID(-1);

    private static Map<Integer,EventType> valueMap = new HashMap<>();
    static {
        for (EventType type: EventType.values()) {
            valueMap.put(type.value, type);
        }
    }

    int value;
    EventType(int aValue) {
        value = aValue;
    }

    public int value() {
        return value;
    }

    public static EventType fromValue(int value) {
        if (!valueMap.containsKey(value)) {
            return EventType.INVALID;
        }

        return valueMap.get(value);
    }
}
