package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.jobs.metricsRollup.aggregator.store.event.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SessionAggregatorTest extends AbstractAggregatorTest {
    @Test
    @DisplayName("it should aggregate the session and active user metrics")
    void sessionMetricsAggregateTest() {
        final MockEventStore store = new MockEventStore(spark);
        final SessionAggregator aggregator = new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_DAILY, null);
        final Dataset<Row> metricsDS =  aggregator.transform(store.scan(), new HiveTime(2018, 4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the session and active user metrics")
    void sessionMetricsAggregateMonthlyTest() {
        final MockEventStore store = new MockEventStore(spark);
        final SessionAggregator aggregator = new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_MONTHLY, null);
        final Dataset<Row> metricsDS =  aggregator.transform(store.scan(), new HiveTime(2018, 4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the session and active user metrics segmentValue to Country Code")
    void sessionMetricsCCAggregationTest() {
        final MockEventStoreSegmentCountryCode store = new MockEventStoreSegmentCountryCode(spark);
        final SessionAggregator aggregator = new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_DAILY, HiveDefines.MetricSegments.SEGMENT_COUNTRY_CODE);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the session and active user metrics segmentValue to platform")
    void sessionMetricsPlatformAggregationTest() {
        final MockEventStoreSegmentPlatform store = new MockEventStoreSegmentPlatform(spark);
        final SessionAggregator aggregator = new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_DAILY, HiveDefines.MetricSegments.SEGMENT_PLATFORM);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the session and active user metrics segmentValue to build")
    void sessionMetricsBuildAggregationTest() {
        final MockEventStoreSegmentBuild store = new MockEventStoreSegmentBuild(spark);
        final SessionAggregator aggregator = new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_DAILY, HiveDefines.MetricSegments.SEGMENT_BUILD);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the session and active user metrics segmentValue to age")
    void sessionMetricsAgeAggregationTest() {
        final MockEventStoreSegmentAge store = new MockEventStoreSegmentAge(spark);
        final SessionAggregator aggregator = new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_DAILY, HiveDefines.MetricSegments.SEGMENT_AGE);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the session and active user metrics segmentValue to spend")
    void sessionMetricsSpendAggregationTest() {
        final MockEventStoreSegmentSpend store = new MockEventStoreSegmentSpend(spark);
        final SessionAggregator aggregator = new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_DAILY, HiveDefines.MetricSegments.SEGMENT_SPEND);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the session and active user metrics segmentValue to custom01")
    void sessionMetricsCustom01AggregationTest() {
        final MockEventStoreSegmentCustom01 store = new MockEventStoreSegmentCustom01(spark);
        final SessionAggregator aggregator = new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_DAILY, HiveDefines.MetricSegments.SEGMENT_CUSTOM_01);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the session and active user metrics segmentValue to custom02")
    void sessionMetricsSegment02AggregationTest() {
        final MockEventStoreSegmentCustom02 store = new MockEventStoreSegmentCustom02(spark);
        final SessionAggregator aggregator = new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_DAILY, HiveDefines.MetricSegments.SEGMENT_CUSTOM_02);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the session and active user metrics segmentValue to custom03")
    void sessionMetricsCustom03AggregationTest() {
        final MockEventStoreSegmentCustom03 store = new MockEventStoreSegmentCustom03(spark);
        final SessionAggregator aggregator = new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_DAILY, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should not run transform for invalid data set")
    void invalidTransformDataSetTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final SessionAggregator aggregator = new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_DAILY, null);
            aggregator.transform(null, new HiveTime(2018, 4, 16));
        });
    }

    @Test
    @DisplayName("it should not run transform for invalid time")
    void invalidTransformTimeTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final SessionAggregator aggregator = new SessionAggregator(HiveDefines.MetricId.USER_SESSIONS_DAILY, null);
            aggregator.transform(new MockEventStore(spark).scan(), null);
        });
    }
}
