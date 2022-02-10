package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.jobs.metricsRollup.aggregator.store.event.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SessionAttributeAggregatorTest extends AbstractAggregatorTest {
    @Test
    @DisplayName("it should aggregate the session and active user metrics")
    void sessionMetricsAggregateTest() {
        final MockEventStore store = new MockEventStore(spark);
        final SessionAttributeAggregator aggregator = new SessionAttributeAggregator(spark, HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_DAILY, null);
        final Dataset<Row> metricsDS =  aggregator.transform(store.scan(), new HiveTime(2018, 4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the session and active user metrics")
    void sessionMetricsAggregateMonthlyTest() {
        final MockEventStore store = new MockEventStore(spark);
        final SessionAttributeAggregator aggregator = new SessionAttributeAggregator(spark, HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_MONTHLY, null);
        final Dataset<Row> metricsDS =  aggregator.transform(store.scan(), new HiveTime(2018, 4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the session and active user metrics segmentValue to platform")
    void sessionMetricsPlatformAggregationTest() {
        final MockEventStoreSegmentPlatform store = new MockEventStoreSegmentPlatform(spark);
        final SessionAttributeAggregator aggregator = new SessionAttributeAggregator(spark, HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_DAILY, HiveDefines.MetricSegments.SEGMENT_PLATFORM);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the session and active user metrics segmentValue to spend")
    void sessionMetricsSpendAggregationTest() {
        final MockEventStoreSegmentSpend store = new MockEventStoreSegmentSpend(spark);
        final SessionAttributeAggregator aggregator = new SessionAttributeAggregator(spark, HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_DAILY, HiveDefines.MetricSegments.SEGMENT_SPEND);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should not run transform for invalid data set")
    void invalidTransformDataSetTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final SessionAttributeAggregator aggregator = new SessionAttributeAggregator(spark, HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_DAILY, null);
            aggregator.transform(null, new HiveTime(2018, 4, 16));
        });
    }

    @Test
    @DisplayName("it should not run transform for invalid time")
    void invalidTransformTimeTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final SessionAttributeAggregator aggregator = new SessionAttributeAggregator(spark, HiveDefines.MetricId.USER_SESSIONS_ATTRIBUTES_DAILY, null);
            aggregator.transform(new MockEventStore(spark).scan(), null);
        });
    }
}
