package io.fizz.gateway.http.annotations;

public enum AuthScheme {
    DIGEST("digest"),
    SESSION_TOKEN("session_token"),
    NONE("none");

    private final String value;

    AuthScheme(final String aValue) {
        this.value = aValue;
    }

    public String value() {
        return value;
    }
}
