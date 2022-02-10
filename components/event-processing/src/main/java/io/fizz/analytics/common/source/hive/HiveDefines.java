package io.fizz.analytics.common.source.hive;

public interface HiveDefines {
    interface ValueTag {
       String COUNT = "count";
       String SUM = "sum";
       String MEAN = "mean";
       String MIN = "min";
       String MAX = "max";
    }

    interface MetricSegments {
        String SEGMENT_NONE = null;
        String SEGMENT_COUNTRY_CODE = HiveProfileEnrichedEventTableSchema.COL_COUNTRY_CODE.title();
        String SEGMENT_PLATFORM = HiveProfileEnrichedEventTableSchema.COL_PLATFORM.title();
        String SEGMENT_BUILD = HiveProfileEnrichedEventTableSchema.COL_BUILD.title();
        String SEGMENT_AGE = HiveProfileEnrichedEventTableSchema.COL_AGE.title();
        String SEGMENT_SPEND = HiveProfileEnrichedEventTableSchema.COL_SPEND.title();
        String SEGMENT_CUSTOM_01 = HiveProfileEnrichedEventTableSchema.COL_CUSTOM_01.title();
        String SEGMENT_CUSTOM_02 = HiveProfileEnrichedEventTableSchema.COL_CUSTOM_02.title();
        String SEGMENT_CUSTOM_03 = HiveProfileEnrichedEventTableSchema.COL_CUSTOM_03.title();


        String[] METRIC_ALL_SEGMENTS = new String[] {
                SEGMENT_NONE,
                SEGMENT_COUNTRY_CODE,
                SEGMENT_PLATFORM,
                SEGMENT_BUILD,
                SEGMENT_CUSTOM_01,
                SEGMENT_CUSTOM_02,
                SEGMENT_CUSTOM_03,
                SEGMENT_AGE,
                SEGMENT_SPEND
        };
    }

    enum MetricId {
        NEW_USERS_DAILY("newUsersD"),
        NEW_USERS_MONTHLY("newUsersM"),
        CHAT_MSGS_DAILY("chatMsgsD"),
        CHAT_MSGS_MONTHLY("chatMsgsM"),
        ACTIVE_USERS_DAILY("activeUsersD"),
        ACTIVE_PAYING_USER_USERS_DAILY("activeUsersDPU"),
        ACTIVE_USERS_MONTHLY("activeUsersM"),
        ACTIVE_USERS_MONTHLY_BILLING("activeUsersMB"),
        ACTIVE_USERS_MID_MONTHLY_BILLING("activeUsersMMB"),
        USER_SESSIONS_DAILY("sessionsD"),
        USER_SESSIONS_MONTHLY("sessionsM"),
        USER_SESSIONS_ATTRIBUTES_DAILY("sessionsAD"),
        USER_SESSIONS_ATTRIBUTES_MONTHLY("sessionsAM"),
        TRANSLATION_COUNT_DAILY("transD"),
        TRANSLATION_COUNT_MONTHLY("transM"),
        CHARS_TRANSLATED_DAILY("transCharsD"),
        CHARS_TRANSLATED_MONTHLY("transCharsM"),
        CHARS_TRANSLATED_MONTHLY_BILLING("transCharsMB"),
        CHARS_TRANSLATED_MID_MONTHLY_BILLING("transCharsMMB"),
        SENTIMENT_NEGATIVE_DAILY("sentNgD"),
        SENTIMENT_NEUTRAL_DAILY("sentNtD"),
        SENTIMENT_POSITIVE_DAILY("sentPvD"),
        SENTIMENT_NEGATIVE_MONTHLY("sentNgM"),
        SENTIMENT_NEUTRAL_MONTHLY("sentNtM"),
        SENTIMENT_POSITIVE_MONTHLY("sentPvM"),
        SENTIMENT_DAILY("sentD"),
        SENTIMENT_MONTHLY("sentM"),
        REVENUE_DAILY("revenueD"),
        REVENUE_MONTHLY("revenueM");

        private final String value;
        MetricId(final String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    enum ValueType {
        Long,
        Double
    }

    interface SENTIMENT_FIELDS {
        String SCORE = "score";
        String TEXT = "text";
        String KEYWORDS = "keywords";
    }
}
