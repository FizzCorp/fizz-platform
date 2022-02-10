package io.fizz.gateway.http.annotations;

public enum RLScope {
    APP("app"),
    CHANNEL("channel"),
    USER("user"),
    NONE("none");

    private final String value;

    RLScope(final String aValue) {
        value = aValue;
    }

    public String value() {
        return value;
    }
}
