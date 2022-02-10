package io.fizz.common;

public enum NewRelicErrorPriority {
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low");

    private String value;
    NewRelicErrorPriority(String aValue) {
        value = aValue;
    }

    public String value() {
        return value;
    }
}
