package io.fizz.analytics.common.source.hive;

public enum HiveEventFields {
    SESSION_LENGTH("len"),
    TEXT_MESSAGE_CONTENT("content"),
    TEXT_MESSAGE_CHANNEL("channel"),
    TEXT_MESSAGE_NICK("nick"),
    TRANS_LANG_FROM("from"),
    TRANS_LANG_TO("to"),
    TRANS_TEXT_LEN("len"),
    TRANS_TEXT_MESSAGE_ID("messageId"),
    PURCHASE_PRODUCT_ID("pid"),
    PURCHASE_AMOUNT("amount"),
    PURCHASE_RECEIPT("receipt"),
    SENTIMENT_SCORE("sentScore"),
    TEXT_MESSAGE_KEYWORDS("keywords"),
    PROFILE_OLDEST_ACTIVITY_TS("oldestTS"),
    PROFILE_LATEST_ACTIVITY_TS("latestTS"),
    PROFILE_SPENT("spent"),
    PROFILE_SENTIMENT_SUM("sentimentSum"),
    PROFILE_MESSAGE_COUNT("messageCount");

    private final String value;
    HiveEventFields(final String aValue) {
        value = aValue;
    }

    public String value() {
        return value;
    }
}
