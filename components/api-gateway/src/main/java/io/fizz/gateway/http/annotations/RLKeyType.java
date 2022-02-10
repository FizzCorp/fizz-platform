package io.fizz.gateway.http.annotations;

public enum RLKeyType {
    PATH("path"),
    BODY("body");

    private final String value;

    RLKeyType(final String aValue) {
        value = aValue;
    }

    public String value() {
        return value;
    }
}
