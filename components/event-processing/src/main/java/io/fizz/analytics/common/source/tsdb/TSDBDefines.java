package io.fizz.analytics.common.source.tsdb;

public class TSDBDefines {
    public static final String TAG_APP_ID = "appId";
    public static final String TAG_VALUE_ANY = "any";

    public static class Metric {
        public static final String TAG_NEW_USERS_COUNT_1MIN = "newUsersCount1Min";
        public static final String TAG_NEW_USERS_COUNT_DAILY = "newUsersCountDaily";
        public static final String TAG_NEW_USERS_COUNT_MONTHLY = "newUsersCountMonthly";

        public static final String TAG_USERS_ACTIVE_COUNT_1MIN = "activeUsersCount1Min";
        public static final String TAG_USERS_ACTIVE_COUNT_DAILY = "activeUsersCountDaily";
        public static final String TAG_USERS_ACTIVE_PAYING_COUNT_DAILY = "activePayingUsersCountDaily";
        public static final String TAG_USERS_ACTIVE_COUNT_MONTHLY = "activeUsersCountMonthly";
        public static final String TAG_USERS_ACTIVE_COUNT_MONTHLY_BILLING = "activeUsersCountMonthlyBilling";
        public static final String TAG_USERS_ACTIVE_COUNT_MID_MONTHLY_BILLING = "activeUsersCountMidMonthlyBilling";

        public static final String TAG_CHAT_MSGS_COUNT_1MIN = "chatMessagesCount1Min";
        public static final String TAG_CHAT_MSGS_COUNT_DAILY = "chatMessagesCountDaily";
        public static final String TAG_CHAT_MSGS_COUNT_MONTHLY = "chatMessagesCountMonthly";

        public static final String TAG_TEXT_TRANSLATED_1MIN = "textTranslated1Min";
        public static final String TAG_TEXT_TRANSLATED_DAILY = "textTranslatedDaily";
        public static final String TAG_TEXT_TRANSLATED_MONTHLY = "textTranslatedMonthly";

        public static final String TAG_CHARS_TRANSLATED_1MIN = "charsTranslated1Min";
        public static final String TAG_CHARS_TRANSLATED_DAILY = "charsTranslatedDaily";
        public static final String TAG_CHARS_TRANSLATED_MONTHLY = "charsTranslatedMonthly";
        public static final String TAG_CHARS_TRANSLATED_MONTHLY_BILLING = "charsTranslatedMonthlyBilling";
        public static final String TAG_CHARS_TRANSLATED_MID_MONTHLY_BILLING = "charsTranslatedMidMonthlyBilling";

        // 1 min sessions
        public static final String TAG_USER_SESSIONS_STARTED_1MIN = "userSessionsStarted1Min";

        // daily sessions
        public static final String TAG_USER_SESSIONS_COUNT_DAILY = "userSessionsCountDaily";
        public static final String TAG_USER_SESSIONS_DUR_TOTAL_DAILY = "userSessionsDurTotalDaily";
        public static final String TAG_USER_SESSIONS_DUR_MEAN_DAILY = "userSessionsDurMeanDaily";
        public static final String TAG_USER_SESSIONS_DUR_MIN_DAILY = "userSessionsDurMinDaily";
        public static final String TAG_USER_SESSIONS_DUR_MAX_DAILY = "userSessionsDurMaxDaily";

        // monthly sessions
        public static final String TAG_USER_SESSIONS_COUNT_MONTHLY = "userSessionsCountMonthly";
        public static final String TAG_USER_SESSIONS_DUR_TOTAL_MONTHLY = "userSessionsDurTotalMonthly";
        public static final String TAG_USER_SESSIONS_DUR_MEAN_MONTHLY = "userSessionsDurMeanMonthly";
        public static final String TAG_USER_SESSIONS_DUR_MIN_MONTHLY = "userSessionsDurMinMonthly";
        public static final String TAG_USER_SESSIONS_DUR_MAX_MONTHLY = "userSessionsDurMaxMonthly";

        // sentiment score daily
        public static final String TAG_SENTIMENT_MEAN_DAILY = "sentimentMeanDaily";
        public static final String TAG_SENTIMENT_MIN_DAILY = "sentimentMaxDaily";
        public static final String TAG_SENTIMENT_MAX_DAILY = "sentimentMinDaily";
        public static final String TAG_SENTIMENT_NEGATIVE_COUNT_DAILY = "sentimentNegativeCountDaily";
        public static final String TAG_SENTIMENT_POSITIVE_COUNT_DAILY = "sentimentPositiveCountDaily";
        public static final String TAG_SENTIMENT_NEUTRAL_COUNT_DAILY = "sentimentNeutralCountDaily";

        // sentiment score monthly
        public static final String TAG_SENTIMENT_MEAN_MONTHLY = "sentimentMeanMonthly";
        public static final String TAG_SENTIMENT_MIN_MONTHLY = "sentimentMaxMonthly";
        public static final String TAG_SENTIMENT_MAX_MONTHLY = "sentimentMinMonthly";
        public static final String TAG_SENTIMENT_NEGATIVE_COUNT_MONTHLY = "sentimentNegativeCountMonthly";
        public static final String TAG_SENTIMENT_POSITIVE_COUNT_MONTHLY = "sentimentPositiveCountMonthly";
        public static final String TAG_SENTIMENT_NEUTRAL_COUNT_MONTHLY = "sentimentNeutralCountMonthly";

        // sentiment score one minute
        public static final String TAG_SENTIMENT_NEGATIVE_COUNT_1MIN = "sentimentNegativeCount1Min";
        public static final String TAG_SENTIMENT_POSITIVE_COUNT_1MIN = "sentimentPositiveCount1Min";
        public static final String TAG_SENTIMENT_NEUTRAL_COUNT_1MIN = "sentimentNeutralCount1Min";

        // revenue
        public static final String TAG_REVENUE_SUM_DAILY = "revenueSumDaily";
        public static final String TAG_REVENUE_MIN_DAILY = "revenueMinDaily";
        public static final String TAG_REVENUE_MAX_DAILY = "revenueMaxDaily";
        public static final String TAG_REVENUE_SUM_MONTHLY = "revenueSumMonthly";
        public static final String TAG_REVENUE_MIN_MONTHLY = "revenueMinMonthly";
        public static final String TAG_REVENUE_MAX_MONTHLY = "revenueMaxMonthly";

    }

    public static class SegmentDefaults {
        public static final String TAG_VALUE_GEO = "other";
    }
}