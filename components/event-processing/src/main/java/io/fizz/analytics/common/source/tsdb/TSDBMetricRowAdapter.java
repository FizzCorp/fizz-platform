package io.fizz.analytics.common.source.tsdb;

import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.opentsdb.TSDBModels;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import io.fizz.common.MetricSegmentType;
import io.fizz.common.Utils;
import org.apache.spark.sql.Row;

import java.text.SimpleDateFormat;
import java.util.*;

public class TSDBMetricRowAdapter {
    @FunctionalInterface
    interface Adapter {
        List<TSDBModels.DataPoint> run(Row row) throws Exception;
    }

    private static LoggingService.Log logger = LoggingService.getLogger(TSDBMetricRowAdapter.class);

    private final Map<String, Adapter> exporters = new HashMap<>();
    private static final Map<String, String> segments = new HashMap<String, String>() {
        {
            put(TSDBDefines.TAG_VALUE_ANY, MetricSegmentType.TAG_ANY.value());
            put(HiveProfileEnrichedEventTableSchema.COL_COUNTRY_CODE.title(),MetricSegmentType.TAG_GEO.value());
            put(HiveProfileEnrichedEventTableSchema.COL_PLATFORM.title(),MetricSegmentType.TAG_PLATFORM.value());
            put(HiveProfileEnrichedEventTableSchema.COL_BUILD.title(),MetricSegmentType.TAG_BUILD.value());
            put(HiveProfileEnrichedEventTableSchema.COL_CUSTOM_01.title(),MetricSegmentType.TAG_CUSTOM01.value());
            put(HiveProfileEnrichedEventTableSchema.COL_CUSTOM_02.title(),MetricSegmentType.TAG_CUSTOM02.value());
            put(HiveProfileEnrichedEventTableSchema.COL_CUSTOM_03.title(),MetricSegmentType.TAG_CUSTOM03.value());
            put(HiveProfileEnrichedEventTableSchema.COL_AGE.title(),MetricSegmentType.TAG_AGE.value());
            put(HiveProfileEnrichedEventTableSchema.COL_SPEND.title(),MetricSegmentType.TAG_SPEND.value());
        }
    };

    private static final Map<String, String> segmentValues = new HashMap<String, String>() {
        {
            put("??",TSDBDefines.SegmentDefaults.TAG_VALUE_GEO);
        }
    };

    public TSDBMetricRowAdapter() {
        register(HiveDefines.MetricId.NEW_USERS_DAILY.value(), TSDBMetricRowAdapter::onNewUsersDaily);
        register(HiveDefines.MetricId.NEW_USERS_MONTHLY.value(), TSDBMetricRowAdapter::onNewUsersMonthly);

        register(HiveDefines.MetricId.USER_SESSIONS_DAILY.value(), TSDBMetricRowAdapter::onUserSessionsDaily);
        register(HiveDefines.MetricId.USER_SESSIONS_MONTHLY.value(), TSDBMetricRowAdapter::onUserSessionsMonthly);

        register(HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_DAILY.value(), TSDBMetricRowAdapter::onUserSessionsAttributesDaily);
        register(HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_MONTHLY.value(), TSDBMetricRowAdapter::onUserSessionsAttributesMonthly);

        register(HiveDefines.MetricId.ACTIVE_USERS_DAILY.value(), TSDBMetricRowAdapter::onUsersActiveDaily);
        register(HiveDefines.MetricId.ACTIVE_PAYING_USER_USERS_DAILY.value(), TSDBMetricRowAdapter::onPayingUsersActiveDaily);
        register(HiveDefines.MetricId.ACTIVE_USERS_MONTHLY.value(), TSDBMetricRowAdapter::onUsersActiveMonthly);
        register(HiveDefines.MetricId.ACTIVE_USERS_MONTHLY_BILLING.value(), TSDBMetricRowAdapter::onUsersActiveMonthlyBilling);
        register(HiveDefines.MetricId.ACTIVE_USERS_MID_MONTHLY_BILLING.value(), TSDBMetricRowAdapter::onUsersActiveMidMonthlyBilling);

        register(HiveDefines.MetricId.CHAT_MSGS_DAILY.value(), TSDBMetricRowAdapter::onChatMessagesDaily);
        register(HiveDefines.MetricId.CHAT_MSGS_MONTHLY.value(), TSDBMetricRowAdapter::onChatMessagesMonthly);

        register(HiveDefines.MetricId.TRANSLATION_COUNT_DAILY.value(), TSDBMetricRowAdapter::onTextTranslatedDaily);
        register(HiveDefines.MetricId.TRANSLATION_COUNT_MONTHLY.value(), TSDBMetricRowAdapter::onTextTranslatedMonthly);

        register(HiveDefines.MetricId.CHARS_TRANSLATED_DAILY.value(), TSDBMetricRowAdapter::onCharsTranslatedDaily);
        register(HiveDefines.MetricId.CHARS_TRANSLATED_MONTHLY.value(), TSDBMetricRowAdapter::onCharsTranslatedMonthly);
        register(HiveDefines.MetricId.CHARS_TRANSLATED_MONTHLY_BILLING.value(), TSDBMetricRowAdapter::onCharsTranslatedMonthlyBilling);
        register(HiveDefines.MetricId.CHARS_TRANSLATED_MID_MONTHLY_BILLING.value(), TSDBMetricRowAdapter::onCharsTranslatedMidMonthlyBilling);

        register(HiveDefines.MetricId.SENTIMENT_NEGATIVE_DAILY.value(), TSDBMetricRowAdapter::onSentimentNegativeDaily);
        register(HiveDefines.MetricId.SENTIMENT_POSITIVE_DAILY.value(), TSDBMetricRowAdapter::onSentimentPositiveDaily);
        register(HiveDefines.MetricId.SENTIMENT_NEUTRAL_DAILY.value(), TSDBMetricRowAdapter::onSentimentNeutralDaily);

        register(HiveDefines.MetricId.SENTIMENT_NEGATIVE_MONTHLY.value(), TSDBMetricRowAdapter::onSentimentNegativeMonthly);
        register(HiveDefines.MetricId.SENTIMENT_POSITIVE_MONTHLY.value(), TSDBMetricRowAdapter::onSentimentPositiveMonthly);
        register(HiveDefines.MetricId.SENTIMENT_NEUTRAL_MONTHLY.value(), TSDBMetricRowAdapter::onSentimentNeutralMonthly);

        register(HiveDefines.MetricId.SENTIMENT_DAILY.value(), TSDBMetricRowAdapter::onSentimentDaily);
        register(HiveDefines.MetricId.SENTIMENT_MONTHLY.value(), TSDBMetricRowAdapter::onSentimentMonthly);

        register(HiveDefines.MetricId.REVENUE_DAILY.value(), TSDBMetricRowAdapter::onRevenueDaily);
        register(HiveDefines.MetricId.REVENUE_MONTHLY.value(), TSDBMetricRowAdapter::onRevenueMonthly);
    }

    public List<TSDBModels.DataPoint> run(Row row) throws Exception {
        final int fieldIdx = row.fieldIndex(HiveMetricTableSchema.COL_TYPE.title());
        final String type = row.getString(fieldIdx);
        final Adapter adapter = exporters.get(type);
        if (adapter == null) {
            logger.warn("Unknown metric type specified: " + type);
            return null;
        }

        return adapter.run(row);
    }

    private void register(String key, Adapter adapter) {
        exporters.put(key, adapter);
    }

    private static List<TSDBModels.DataPoint> onNewUsersDaily(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_NEW_USERS_COUNT_DAILY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onNewUsersMonthly(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_NEW_USERS_COUNT_MONTHLY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onUsersActiveDaily(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_DAILY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onPayingUsersActiveDaily(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_USERS_ACTIVE_PAYING_COUNT_DAILY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onUsersActiveMonthly(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_MONTHLY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onUsersActiveMonthlyBilling(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_MONTHLY_BILLING, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onUsersActiveMidMonthlyBilling(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_MID_MONTHLY_BILLING, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onChatMessagesDaily(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_CHAT_MSGS_COUNT_DAILY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onChatMessagesMonthly(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_CHAT_MSGS_COUNT_MONTHLY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onTextTranslatedDaily(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_TEXT_TRANSLATED_DAILY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onTextTranslatedMonthly(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_TEXT_TRANSLATED_MONTHLY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onCharsTranslatedDaily(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.SUM, TSDBDefines.Metric.TAG_CHARS_TRANSLATED_DAILY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onCharsTranslatedMonthly(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.SUM, TSDBDefines.Metric.TAG_CHARS_TRANSLATED_MONTHLY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onCharsTranslatedMonthlyBilling(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.SUM, TSDBDefines.Metric.TAG_CHARS_TRANSLATED_MONTHLY_BILLING, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onCharsTranslatedMidMonthlyBilling(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.SUM, TSDBDefines.Metric.TAG_CHARS_TRANSLATED_MID_MONTHLY_BILLING, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onUserSessionsDaily(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_USER_SESSIONS_COUNT_DAILY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onUserSessionsMonthly(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_USER_SESSIONS_COUNT_MONTHLY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onUserSessionsAttributesDaily(Row row) throws Exception {
        final List<TSDBModels.DataPoint> req = new ArrayList<>();

        req.add(toDP(row, HiveDefines.ValueTag.SUM, TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_TOTAL_DAILY, HiveDefines.ValueType.Double));
        req.add(toDP(row, HiveDefines.ValueTag.MEAN, TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_MEAN_DAILY, HiveDefines.ValueType.Double));
        req.add(toDP(row, HiveDefines.ValueTag.MIN, TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_MIN_DAILY, HiveDefines.ValueType.Double));
        req.add(toDP(row, HiveDefines.ValueTag.MAX, TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_MAX_DAILY, HiveDefines.ValueType.Double));

        return req;
    }

    private static List<TSDBModels.DataPoint> onUserSessionsAttributesMonthly(Row row) throws Exception {
        final List<TSDBModels.DataPoint> req = new ArrayList<>();

        req.add(toDP(row, HiveDefines.ValueTag.SUM, TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_TOTAL_MONTHLY, HiveDefines.ValueType.Double));
        req.add(toDP(row, HiveDefines.ValueTag.MEAN, TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_MEAN_MONTHLY, HiveDefines.ValueType.Double));
        req.add(toDP(row, HiveDefines.ValueTag.MIN, TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_MIN_MONTHLY, HiveDefines.ValueType.Double));
        req.add(toDP(row, HiveDefines.ValueTag.MAX, TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_MAX_MONTHLY, HiveDefines.ValueType.Double));

        return req;
    }

    private static List<TSDBModels.DataPoint> onSentimentNegativeDaily(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_SENTIMENT_NEGATIVE_COUNT_DAILY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onSentimentPositiveDaily(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_SENTIMENT_POSITIVE_COUNT_DAILY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onSentimentNeutralDaily(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_SENTIMENT_NEUTRAL_COUNT_DAILY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onSentimentNegativeMonthly(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_SENTIMENT_NEGATIVE_COUNT_MONTHLY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onSentimentPositiveMonthly(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_SENTIMENT_POSITIVE_COUNT_MONTHLY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onSentimentNeutralMonthly(Row row) throws Exception {
        TSDBModels.DataPoint dp = toDP(row, HiveDefines.ValueTag.COUNT, TSDBDefines.Metric.TAG_SENTIMENT_NEUTRAL_COUNT_MONTHLY, HiveDefines.ValueType.Double);
        return new ArrayList<TSDBModels.DataPoint>() {
            { add(dp); }
        };
    }

    private static List<TSDBModels.DataPoint> onSentimentDaily(Row row) throws Exception {
        final List<TSDBModels.DataPoint> req = new ArrayList<>();

        req.add(toDP(row, HiveDefines.ValueTag.MEAN, TSDBDefines.Metric.TAG_SENTIMENT_MEAN_DAILY, HiveDefines.ValueType.Double));
        req.add(toDP(row, HiveDefines.ValueTag.MIN, TSDBDefines.Metric.TAG_SENTIMENT_MIN_DAILY, HiveDefines.ValueType.Double));
        req.add(toDP(row, HiveDefines.ValueTag.MAX, TSDBDefines.Metric.TAG_SENTIMENT_MAX_DAILY, HiveDefines.ValueType.Double));

        return req;
    }

    private static List<TSDBModels.DataPoint> onSentimentMonthly(Row row) throws Exception {
        final List<TSDBModels.DataPoint> req = new ArrayList<>();

        req.add(toDP(row, HiveDefines.ValueTag.MEAN, TSDBDefines.Metric.TAG_SENTIMENT_MEAN_MONTHLY, HiveDefines.ValueType.Double));
        req.add(toDP(row, HiveDefines.ValueTag.MIN, TSDBDefines.Metric.TAG_SENTIMENT_MIN_MONTHLY, HiveDefines.ValueType.Double));
        req.add(toDP(row, HiveDefines.ValueTag.MAX, TSDBDefines.Metric.TAG_SENTIMENT_MAX_MONTHLY, HiveDefines.ValueType.Double));

        return req;
    }

    private static List<TSDBModels.DataPoint> onRevenueDaily(Row row) throws Exception {
        final List<TSDBModels.DataPoint> req = new ArrayList<>();

        req.add(toDP(row, HiveDefines.ValueTag.SUM, TSDBDefines.Metric.TAG_REVENUE_SUM_DAILY, HiveDefines.ValueType.Long));
        req.add(toDP(row, HiveDefines.ValueTag.MIN, TSDBDefines.Metric.TAG_REVENUE_MIN_DAILY, HiveDefines.ValueType.Long));
        req.add(toDP(row, HiveDefines.ValueTag.MAX, TSDBDefines.Metric.TAG_REVENUE_MAX_DAILY, HiveDefines.ValueType.Long));

        return req;
    }

    private static List<TSDBModels.DataPoint> onRevenueMonthly(Row row) throws Exception {
        final List<TSDBModels.DataPoint> req = new ArrayList<>();

        req.add(toDP(row, HiveDefines.ValueTag.SUM, TSDBDefines.Metric.TAG_REVENUE_SUM_MONTHLY, HiveDefines.ValueType.Long));
        req.add(toDP(row, HiveDefines.ValueTag.MIN, TSDBDefines.Metric.TAG_REVENUE_MIN_MONTHLY, HiveDefines.ValueType.Long));
        req.add(toDP(row, HiveDefines.ValueTag.MAX, TSDBDefines.Metric.TAG_REVENUE_MAX_MONTHLY, HiveDefines.ValueType.Long));

        return req;
    }

    private static TSDBModels.DataPoint toDP(Row row, String valueTag, String metricTag, HiveDefines.ValueType valueType) throws Exception {
        final Date time = new Date(getMetricTimestamp(row));
        final int fieldIdx = row.fieldIndex(HiveMetricTableSchema.COL_APP_ID.title());
        final String appId = row.getString(fieldIdx);
        final int valueIdx = getColIdx(row, valueTag) + 1;

        final int dimIdx = row.fieldIndex(HiveMetricTableSchema.COL_DIM.title());
        final int dimValueIdx = row.fieldIndex(HiveMetricTableSchema.COL_DIM_VALUE.title());
        final String dim = row.getString(dimIdx);
        final String dimValue = row.getString(dimValueIdx);

        final String segment = segments.get(dim);
        final String segmentValue = segmentValues.getOrDefault(dimValue, dimValue);
        final String hashedSegmentValue = Utils.tsdbSegment(segmentValue);

        if (valueIdx < 0 || valueIdx >= row.size()) {
            throw new Exception(String.format("Invalid Format: Could not find value key: %s.", valueTag));
        }

        Object value = null;
        if (valueType == HiveDefines.ValueType.Long) {
            try {
                value = Long.parseLong(row.getString(valueIdx));
            }
            catch (Exception ex) {
                logger.warn(String.format("Exception while parsing integer: %s converting to 0", row.getString(valueIdx)));
                value = 0;
            }
        }
        else
        if (valueType == HiveDefines.ValueType.Double) {
            try {
                value = Double.parseDouble(row.getString(valueIdx));
            }
            catch (Exception ex) {
                logger.warn(String.format("Exception while parsing double (appId=%s): %s converting to 0.0", appId, row.getString(valueIdx)));
                value = 0.0;
            }
        }

        return new TSDBModels.DataPointBuilder()
                .metric(metricTag)
                .timestamp(time)
                .value(value)
                .addTag(TSDBDefines.TAG_APP_ID, appId)
                .addTag(segment, hashedSegmentValue)
                .build();
    }

    private static long dateToTimestamp(int year, int month, int day) throws Exception {
        final String timeStr = "" +
                (month < 10 ? "0" + month : month) + "-" +
                (day < 10 ? "0" + day : day) + "-" +
                year;
        final SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        return format.parse(timeStr).getTime();
    }

    private static int safeGetDateElement(final Row row, int colIdx) {
        try {
            return row.getInt(colIdx);
        }
        catch (Exception ex) {
            return 0;
        }
    }

    private static long getMetricTimestamp(final Row row) throws Exception {
        final String year = row.getString(row.fieldIndex(HiveMetricTableSchema.COL_DT_YEAR.title()));
        final String month = row.getString(row.fieldIndex(HiveMetricTableSchema.COL_DT_MONTH.title()));
        final String day = row.getString(row.fieldIndex(HiveMetricTableSchema.COL_DT_DAY.title()));

        return dateToTimestamp(
                Integer.parseInt(year),
                month.equals("any") ? 1 : Integer.parseInt(month),
                day.equals("any") ? 1 : Integer.parseInt(day)
        );
    }

    private static int getColIdx(final Row row, final String key) {
        for (int ci = 0; ci < row.size(); ci++) {
            String cell = row.get(ci).getClass() == String.class ? row.getString(ci) : null;
            if (Objects.equals(cell,key)) {
                return ci;
            }
        }
        return -1;
    }
}
