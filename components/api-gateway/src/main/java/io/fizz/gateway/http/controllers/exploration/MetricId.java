package io.fizz.gateway.http.controllers.exploration;

import java.util.HashMap;
import java.util.Map;

public enum MetricId {
    NEW_USERS_COUNT_1MIN("newUsersCount1Min"),
    NEW_USERS_COUNT_DAILY("newUsersCountDaily"),
    NEW_USERS_COUNT_MONTHLY("newUsersCountMonthly"),

    ACTIVE_USERS_COUNT_1MIN("activeUsersCount1Min"),
    ACTIVE_USERS_COUNT_DAILY("activeUsersCountDaily"),
    ACTIVE_PAYING_USERS_COUNT_DAILY ("activePayingUsersCountDaily"),
    ACTIVE_USERS_COUNT_MONTHLY("activeUsersCountMonthly"),
    ACTIVE_USERS_COUNT_MONTHLY_BILLING("activeUsersCountMonthlyBilling"),
    ACTIVE_USERS_COUNT_MID_MONTHLY_BILLING("activeUsersCountMidMonthlyBilling"),

    CHAT_MESSAGES_COUNT_1MIN("chatMessagesCount1Min"),
    CHAT_MESSAGES_COUNT_DAILY("chatMessagesCountDaily"),
    CHAT_MESSAGES_COUNT_MONTHLY("chatMessagesCountMonthly"),

    CHARS_TRANSLATED_1MIN("charsTranslated1Min"),
    CHARS_TRANSLATED_DAILY("charsTranslatedDaily"),
    CHARS_TRANSLATED_MONTHLY("charsTranslatedMonthly"),
    CHARS_TRANSLATED_MONTHLY_BILLING("charsTranslatedMonthlyBilling"),
    CHARS_TRANSLATED_MID_MONTHLY_BILLING("charsTranslatedMidMonthlyBilling"),

    // 1 min sessions
    USER_SESSIONS_STARTED_1MIN("userSessionsStarted1Min"),

    // daily sessions
    USER_SESSIONS_COUNT_DAILY("userSessionsCountDaily"),
    USER_SESSIONS_DUR_TOTAL_DAILY("userSessionsDurTotalDaily"),
    USER_SESSIONS_DUR_MEAN_DAILY("userSessionsDurMeanDaily"),
    USER_SESSIONS_DUR_MIN_DAILY("userSessionsDurMinDaily"),
    USER_SESSIONS_DUR_MAX_DAILY("userSessionsDurMaxDaily"),

    // monthly sessions
    USER_SESSIONS_COUNT_MONTHLY("userSessionsCountMonthly"),
    USER_SESSIONS_DUR_TOTAL_MONTHLY("userSessionsDurTotalMonthly"),
    USER_SESSIONS_DUR_MEAN_MONTHLY("userSessionsDurMeanMonthly"),
    USER_SESSIONS_DUR_MIN_MONTHLY("userSessionsDurMinMonthly"),
    USER_SESSIONS_DUR_MAX_MONTHLY("userSessionsDurMaxMonthly"),

    // sentiment score daily
    SENTIMENT_NEGATIVE_COUNT_DAILY("sentimentNegativeCountDaily"),
    SENTIMENT_POSITIVE_COUNT_DAILY("sentimentPositiveCountDaily"),
    SENTIMENT_NEUTRAL_COUNT_DAILY("sentimentNeutralCountDaily"),

    // sentiment score monthly
    SENTIMENT_NEGATIVE_COUNT_MONTHLY("sentimentNegativeCountMonthly"),
    SENTIMENT_POSITIVE_COUNT_MONTHLY("sentimentPositiveCountMonthly"),
    SENTIMENT_NEUTRAL_COUNT_MONTHLY("sentimentNeutralCountMonthly"),

    // sentiment score one minute
    SENTIMENT_NEGATIVE_COUNT_1MIN("sentimentNegativeCount1Min"),
    SENTIMENT_POSITIVE_COUNT_1MIN("sentimentPositiveCount1Min"),
    SENTIMENT_NEUTRAL_COUNT_1MIN("sentimentNeutralCount1Min"),

    //new sentiments
    SENTIMENT_MEAN_DAILY("sentimentMeanDaily"),
    SENTIMENT_MIN_DAILY("sentimentMaxDaily"),
    SENTIMENT_MAX_DAILY("sentimentMinDaily"),

    SENTIMENT_MEAN_MONTHLY("sentimentMeanMonthly"),
    SENTIMENT_MIN_MONTHLY("sentimentMaxMonthly"),
    SENTIMENT_MAX_MONTHLY("sentimentMinMonthly"),

    //revenue
    REVENUE_SUM_DAILY("revenueSumDaily"),
    REVENUE_MIN_DAILY("revenueMinDaily"),
    REVENUE_MAX_DAILY("revenueMaxDaily"),

    REVENUE_SUM_MONTHLY("revenueSumMonthly"),
    REVENUE_MIN_MONTHLY("revenueMinMonthly"),
    REVENUE_MAX_MONTHLY("revenueMaxMonthly");

    private static final Map<String,MetricId> valueMap = new HashMap<>();

    static {
        for (final MetricId id: MetricId.values()) {
            valueMap.put(id.value(), id);
        }
    }

    public static MetricId valueBy(final String aValue) {
        return valueMap.get(aValue);
    }

    private final String value;
    MetricId(final String aValue) {
        value = aValue;
    }

    public String value() {
        return value;
    }
}
