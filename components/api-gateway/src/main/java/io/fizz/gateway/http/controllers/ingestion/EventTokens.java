package io.fizz.gateway.http.controllers.ingestion;

public enum EventTokens {
    USER_ID("user_id"),
    TYPE("type"),
    VERSION("ver"),
    SESSION_ID("session_id"),
    TIME("time"),
    PLATFORM("platform"),
    BUILD("build"),
    CUSTOM_01("custom_01"),
    CUSTOM_02("custom_02"),
    CUSTOM_03("custom_03"),
    DURATION("duration"),
    CONTENT("content"),
    CHANNEL_ID("channel_id"),
    NICK("nick"),
    AMOUNT("amount"),
    CURRENCY("currency"),
    SPENDER("spender"),
    PRODUCT_ID("product_id"),
    RECEIPT("receipt");

    private final String value;
    EventTokens(final String aValue){
        value = aValue;
    }

    public String value() {
        return value;
    }
}
