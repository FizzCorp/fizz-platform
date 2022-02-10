package io.fizz.common;

import java.util.HashMap;
import java.util.Map;

public enum MetricSegmentType {
    TAG_ANY("segment.any"),
    TAG_GEO("segment.geo"),
    TAG_PLATFORM("segment.platform"),
    TAG_BUILD("segment.build"),
    TAG_CUSTOM01("segment.custom01"),
    TAG_CUSTOM02("segment.custom02"),
    TAG_CUSTOM03("segment.custom03"),
    TAG_AGE("segment.age"),
    TAG_SPEND("segment.spend");

    private static Map<String,MetricSegmentType> valueMap = new HashMap<>();
    static {
        for (MetricSegmentType type: MetricSegmentType.values()) {
            valueMap.put(type.value, type);
        }
    }

    private String value;
    MetricSegmentType(String aValue) {
        value = aValue;
    }

    public String value() {
        return value;
    }

    public static MetricSegmentType fromValue(String value) {
        return valueMap.get(value);
    }
}
