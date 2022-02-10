package io.fizz.gateway.http.controllers.exploration;

public enum MessageQueryTokens {
    ID("id"),
    APP_ID("app_id"),
    COUNTRY_CODE("country_code"),
    ACTOR_ID("user_id"),
    NICK("nick"),
    CONTENT("content"),
    CHANNEL("channel_id"),
    PLATFORM("platform"),
    BUILD("build"),
    CUSTOM_01("custom_01"),
    CUSTOM_02("custom_02"),
    CUSTOM_03("custom_03"),
    AGE("age"),
    SPENDER("spender"),
    TIME_STAMP("time"),
    SENTIMENT_SCORE("sentiment_score");

    private final String value;
    MessageQueryTokens(final String aValue){
        value = aValue;
    }

    public String value() {
        return value;
    }
}

