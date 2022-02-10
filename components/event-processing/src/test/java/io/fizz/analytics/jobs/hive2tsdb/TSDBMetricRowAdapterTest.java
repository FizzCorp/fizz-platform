package io.fizz.analytics.jobs.hive2tsdb;

import io.fizz.analytics.common.Utils;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.common.source.tsdb.TSDBDefines;
import io.fizz.analytics.common.opentsdb.TSDBModels;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import io.fizz.analytics.common.source.tsdb.TSDBMetricRowAdapter;
import io.fizz.common.MetricSegmentType;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.junit.jupiter.api.Test;

import java.util.*;

public class TSDBMetricRowAdapterTest {
    private static final int DT_YEAR = 2017;
    private static final int DT_MONTH = 11;
    private static final int DT_DAY = 1;
    private static final String APP_ID = "03f90c2231d5";

    @Test
    public void itShouldCreateActiveUsersCountDailyDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_USERS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_DAILY, 2.0, MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateActiveUsersCountMonthlyDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_USERS_MONTHLY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_MONTHLY, 2.0, MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateActiveUsersCountMonthlyBillingDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_USERS_MONTHLY_BILLING.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_MONTHLY_BILLING, 2.0, MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateActiveUsersCountMidMonthlyBillingDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_USERS_MID_MONTHLY_BILLING.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_MID_MONTHLY_BILLING, 2.0, MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateActivePayingUsersCountDailyDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_PAYING_USER_USERS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_PAYING_COUNT_DAILY, 2.0, MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateActiveUsersCountDailyDPSegmentGeo() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_USERS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .dim(HiveProfileEnrichedEventTableSchema.COL_COUNTRY_CODE.title())
                .dimValue("PK")
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_DAILY, 2.0,MetricSegmentType.TAG_GEO.value(), "PK");
    }

    @Test
    public void itShouldCreateActiveUsersCountDailyDPSegmentGeoOther() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_USERS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .dim(HiveProfileEnrichedEventTableSchema.COL_COUNTRY_CODE.title())
                .dimValue("??")
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_DAILY, 2.0,MetricSegmentType.TAG_GEO.value(), TSDBDefines.SegmentDefaults.TAG_VALUE_GEO);
    }

    @Test
    public void itShouldCreateActiveUsersCountDailyDPSegmentPlatform() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_USERS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .dim(HiveProfileEnrichedEventTableSchema.COL_PLATFORM.title())
                .dimValue("android")
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_DAILY, 2.0,MetricSegmentType.TAG_PLATFORM.value(), "android");
    }

    @Test
    public void itShouldCreateActiveUsersCountDailyDPSegmentBuild() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_USERS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .dim(HiveProfileEnrichedEventTableSchema.COL_BUILD.title())
                .dimValue("50")
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_DAILY, 2.0,MetricSegmentType.TAG_BUILD.value(), "50");
    }

    @Test
    public void itShouldCreateActiveUsersCountDailyDPSegmentCustom01() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_USERS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .dim(HiveProfileEnrichedEventTableSchema.COL_CUSTOM_01.title())
                .dimValue("A")
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_DAILY, 2.0,MetricSegmentType.TAG_CUSTOM01.value(), "A");
    }

    @Test
    public void itShouldCreateActiveUsersCountDailyDPSegmentCustom02() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_USERS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .dim(HiveProfileEnrichedEventTableSchema.COL_CUSTOM_02.title())
                .dimValue("B")
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_DAILY, 2.0,MetricSegmentType.TAG_CUSTOM02.value(), "B");
    }

    @Test
    public void itShouldCreateActiveUsersCountDailyDPSegmentCustom03() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_USERS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .dim(HiveProfileEnrichedEventTableSchema.COL_CUSTOM_03.title())
                .dimValue("null")
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_DAILY, 2.0,MetricSegmentType.TAG_CUSTOM03.value(), "null");
    }

    @Test
    public void itShouldCreateActiveUsersCountDailyDPSegmentAge() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_USERS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .dim(HiveProfileEnrichedEventTableSchema.COL_AGE.title())
                .dimValue("days_1_3")
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_DAILY, 2.0,MetricSegmentType.TAG_AGE.value(), "days_1_3");
    }

    @Test
    public void itShouldCreateActiveUsersCountDailyDPSegmentSpend() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.ACTIVE_USERS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .dim(HiveProfileEnrichedEventTableSchema.COL_SPEND.title())
                .dimValue("none")
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USERS_ACTIVE_COUNT_DAILY, 2.0,MetricSegmentType.TAG_SPEND.value(), "none");
    }

    @Test
    public void itShouldCreateNewUsersCountDailyDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.NEW_USERS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_NEW_USERS_COUNT_DAILY, 2.0,MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateNewUsersCountMonthlyDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.NEW_USERS_MONTHLY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_NEW_USERS_COUNT_MONTHLY, 2.0,MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateChatMessagesCountDailyDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.CHAT_MSGS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_CHAT_MSGS_COUNT_DAILY, 2.0,MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateChatMessagesCountMonthlyDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.CHAT_MSGS_MONTHLY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.COUNT, "2")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert(dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_CHAT_MSGS_COUNT_MONTHLY, 2.0,MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateSessionsDailyDP() throws Exception {
        final Double[] values = {20.0};

        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.USER_SESSIONS_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.COUNT, values[0].toString())
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USER_SESSIONS_COUNT_DAILY, values[0],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateSessionsMonthlyDP() throws Exception {
        final Double[] values = {20.0};

        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.USER_SESSIONS_MONTHLY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.COUNT, values[0].toString())
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USER_SESSIONS_COUNT_MONTHLY, values[0],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateSessionsAttributesDailyDP() throws Exception {
        final Double[] values = {250.0, 15.523, 2.0, 125.223};

        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.SUM, values[0].toString())
                .attribute(HiveDefines.ValueTag.MEAN, values[1].toString())
                .attribute(HiveDefines.ValueTag.MIN, values[2].toString())
                .attribute(HiveDefines.ValueTag.MAX, values[3].toString())
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 4);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_TOTAL_DAILY, values[0],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(1), TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_MEAN_DAILY, values[1],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(2), TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_MIN_DAILY, values[2],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(3), TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_MAX_DAILY, values[3],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateSessionsAttributesMonthlyDP() throws Exception {
        final Double[] values = {250.0, 15.523, 2.0, 125.223};

        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_MONTHLY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.SUM, values[0].toString())
                .attribute(HiveDefines.ValueTag.MEAN, values[1].toString())
                .attribute(HiveDefines.ValueTag.MIN, values[2].toString())
                .attribute(HiveDefines.ValueTag.MAX, values[3].toString())
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 4);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_TOTAL_MONTHLY, values[0],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(1), TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_MEAN_MONTHLY, values[1],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(2), TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_MIN_MONTHLY, values[2],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(3), TSDBDefines.Metric.TAG_USER_SESSIONS_DUR_MAX_MONTHLY, values[3],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateSentimentDailyDP() throws Exception {
        final Double[] values = {0.25, -0.5, 0.5};

        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.SENTIMENT_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.MEAN, values[0].toString())
                .attribute(HiveDefines.ValueTag.MIN, values[1].toString())
                .attribute(HiveDefines.ValueTag.MAX, values[2].toString())
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 3);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_SENTIMENT_MEAN_DAILY, values[0],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(1), TSDBDefines.Metric.TAG_SENTIMENT_MIN_DAILY, values[1],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(2), TSDBDefines.Metric.TAG_SENTIMENT_MAX_DAILY, values[2],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateSentimentMonthlyDP() throws Exception {
        final Double[] values = {0.25, -0.5, 0.5};

        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.SENTIMENT_MONTHLY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.MEAN, values[0].toString())
                .attribute(HiveDefines.ValueTag.MIN, values[1].toString())
                .attribute(HiveDefines.ValueTag.MAX, values[2].toString())
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 3);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_SENTIMENT_MEAN_MONTHLY, values[0],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(1), TSDBDefines.Metric.TAG_SENTIMENT_MIN_MONTHLY, values[1],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(2), TSDBDefines.Metric.TAG_SENTIMENT_MAX_MONTHLY, values[2],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateRevenueDailyDP() throws Exception {
        final Long[] values = {200L, 99L, 101L};

        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.REVENUE_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.SUM, values[0].toString())
                .attribute(HiveDefines.ValueTag.MIN, values[1].toString())
                .attribute(HiveDefines.ValueTag.MAX, values[2].toString())
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 3);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_REVENUE_SUM_DAILY, values[0],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(1), TSDBDefines.Metric.TAG_REVENUE_MIN_DAILY, values[1],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(2), TSDBDefines.Metric.TAG_REVENUE_MAX_DAILY, values[2],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateRevenueMonthlyDP() throws Exception {
        final Long[] values = {200L, 99L, 101L};

        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.REVENUE_MONTHLY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.SUM, values[0].toString())
                .attribute(HiveDefines.ValueTag.MIN, values[1].toString())
                .attribute(HiveDefines.ValueTag.MAX, values[2].toString())
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 3);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_REVENUE_SUM_MONTHLY, values[0],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(1), TSDBDefines.Metric.TAG_REVENUE_MIN_MONTHLY, values[1],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
        validateDP(dataPoints.get(2), TSDBDefines.Metric.TAG_REVENUE_MAX_MONTHLY, values[2],MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateTextTranslationDailyDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.TRANSLATION_COUNT_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.COUNT, "200")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_TEXT_TRANSLATED_DAILY, 200.0, MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateTextTranslationMonthlyDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.TRANSLATION_COUNT_MONTHLY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.COUNT, "200")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_TEXT_TRANSLATED_MONTHLY, 200.0, MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateCharTranslationDailyDP() throws Exception {

        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.CHARS_TRANSLATED_DAILY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.SUM, "2000")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_CHARS_TRANSLATED_DAILY, 2000.0, MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateCharTranslationMonthlyDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.CHARS_TRANSLATED_MONTHLY.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.SUM, "2000")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_CHARS_TRANSLATED_MONTHLY, 2000.0, MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateCharTranslationMonthlyBillingDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.CHARS_TRANSLATED_MONTHLY_BILLING.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.SUM, "2000")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_CHARS_TRANSLATED_MONTHLY_BILLING, 2000.0, MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    @Test
    public void itShouldCreateCharTranslationMidMonthlyBillingDP() throws Exception {
        final Row row = new RowBuilder().metricType(HiveDefines.MetricId.CHARS_TRANSLATED_MID_MONTHLY_BILLING.value())
                .appId(APP_ID)
                .year(DT_YEAR).month(DT_MONTH).day(DT_DAY)
                .attribute(HiveDefines.ValueTag.SUM, "2000")
                .build();

        final TSDBMetricRowAdapter adapter = new TSDBMetricRowAdapter();
        final List<TSDBModels.DataPoint> dataPoints = adapter.run(row);

        assert (dataPoints.size() == 1);
        validateDP(dataPoints.get(0), TSDBDefines.Metric.TAG_CHARS_TRANSLATED_MID_MONTHLY_BILLING, 2000.0, MetricSegmentType.TAG_ANY.value(), TSDBDefines.TAG_VALUE_ANY);
    }

    private void validateDP(final TSDBModels.DataPoint dp, final String metricTag, final Object value,
                            final String segment, final String segmentValue) throws Exception {
        String hashedSegmentValue = io.fizz.common.Utils.tsdbSegment(segmentValue);
        assert (Objects.equals(dp.metric, metricTag));
        assert (Objects.equals(dp.value, value));
        assert (Objects.equals(dp.timestamp, (int)(Utils.dateToTimestamp(DT_YEAR, DT_MONTH, DT_DAY)/1000)));

        assert (Objects.equals(dp.tags.get(TSDBDefines.TAG_APP_ID), APP_ID));
        assert (Objects.equals(dp.tags.get(segment), hashedSegmentValue));
    }

    static private class RowBuilder {
        private String appId;
        private String metricType;
        private String year;
        private String month;
        private String day;
        private String dim = TSDBDefines.TAG_VALUE_ANY;
        private String dimValue = TSDBDefines.TAG_VALUE_ANY;
        private final Map<String,Object> attributes = new HashMap<>();

        RowBuilder metricType(String type) {
            this.metricType = type;
            return this;
        }

        RowBuilder appId(String id) {
            this.appId = id;
            return this;
        }

        RowBuilder dim(String dim) {
            this.dim = dim;
            return this;
        }

        RowBuilder dimValue(String dimValue) {
            this.dimValue = dimValue;
            return this;
        }

        RowBuilder year(final int year) {
            this.year = Integer.toString(year);
            return this;
        }

        RowBuilder month(final int month) {
            this.month = Integer.toString(month);
            return this;
        }

        RowBuilder day(int day) {
            this.day = Integer.toString(day);
            return this;
        }

        RowBuilder attribute(String key, Object value) {
            attributes.put(key, value);
            return this;
        }

        Row build() {
            final int ATTR_COUNT = 5;

            final ArrayList<Object> cellList = new ArrayList<>();
            cellList.add(metricType);

            for (Map.Entry<String,Object> entry: attributes.entrySet()) {
                cellList.add(entry.getKey());
                cellList.add(entry.getValue());
            }

            for (int ai = 0; ai < ATTR_COUNT-attributes.size(); ai++) {
                cellList.add(null);
                cellList.add(null);
            }

            cellList.add(dim); cellList.add(dimValue);
            cellList.add(year); cellList.add(month); cellList.add(day); cellList.add(TSDBDefines.TAG_VALUE_ANY);
            cellList.add(appId);
            cellList.add(year); cellList.add(month); cellList.add(day);

            return new GenericRowWithSchema(cellList.toArray(), new HiveMetricTableSchema().schema());
        }
    }
}
