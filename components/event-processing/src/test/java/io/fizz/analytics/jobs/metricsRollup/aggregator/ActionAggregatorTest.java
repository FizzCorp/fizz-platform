package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.AbstractSparkTest;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.jobs.metricsRollup.aggregator.store.event.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActionAggregatorTest extends AbstractSparkTest {
    @Test
    @DisplayName("it should aggregate the counts of text messages")
    void textMessageCountAggregationTest() {
        final MockEventStore store = new MockEventStore(spark);
        final ActionAggregator aggregator = new ActionAggregator(HiveDefines.MetricId.CHAT_MSGS_DAILY, null);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the counts of text messages segmentValue to Country Code")
    void textMessageCountCCAggregationTest() {
        final MockEventStoreSegmentCountryCode store = new MockEventStoreSegmentCountryCode(spark);
        final ActionAggregator aggregator = new ActionAggregator(HiveDefines.MetricId.CHAT_MSGS_DAILY, HiveDefines.MetricSegments.SEGMENT_COUNTRY_CODE);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the counts of text messages segmentValue to platform")
    void textMessageCountPlatformAggregationTest() {
        final MockEventStoreSegmentPlatform store = new MockEventStoreSegmentPlatform(spark);
        final ActionAggregator aggregator = new ActionAggregator(HiveDefines.MetricId.CHAT_MSGS_DAILY, HiveDefines.MetricSegments.SEGMENT_PLATFORM);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the counts of text messages segmentValue to build")
    void textMessageCountBuildAggregationTest() {
        final MockEventStoreSegmentBuild store = new MockEventStoreSegmentBuild(spark);
        final ActionAggregator aggregator = new ActionAggregator(HiveDefines.MetricId.CHAT_MSGS_DAILY, HiveDefines.MetricSegments.SEGMENT_BUILD);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the counts of text messages segmentValue to age")
    void textMessageCountAgeAggregationTest() {
        final MockEventStoreSegmentAge store = new MockEventStoreSegmentAge(spark);
        final ActionAggregator aggregator = new ActionAggregator(HiveDefines.MetricId.CHAT_MSGS_DAILY, HiveDefines.MetricSegments.SEGMENT_AGE);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the counts of text messages segmentValue to spend")
    void textMessageCountSpendAggregationTest() {
        final MockEventStoreSegmentSpend store = new MockEventStoreSegmentSpend(spark);
        final ActionAggregator aggregator = new ActionAggregator(HiveDefines.MetricId.CHAT_MSGS_DAILY, HiveDefines.MetricSegments.SEGMENT_SPEND);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the counts of text messages segmentValue to custom01")
    void textMessageCountCustom01AggregationTest() {
        final MockEventStoreSegmentCustom01 store = new MockEventStoreSegmentCustom01(spark);
        final ActionAggregator aggregator = new ActionAggregator(HiveDefines.MetricId.CHAT_MSGS_DAILY, HiveDefines.MetricSegments.SEGMENT_CUSTOM_01);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the counts of text messages segmentValue to custom02")
    void textMessageCountSegment02AggregationTest() {
        final MockEventStoreSegmentCustom02 store = new MockEventStoreSegmentCustom02(spark);
        final ActionAggregator aggregator = new ActionAggregator(HiveDefines.MetricId.CHAT_MSGS_DAILY, HiveDefines.MetricSegments.SEGMENT_CUSTOM_02);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the counts of text messages segmentValue to custom03")
    void textMessageCountCustom03AggregationTest() {
        final MockEventStoreSegmentCustom03 store = new MockEventStoreSegmentCustom03(spark);
        final ActionAggregator aggregator = new ActionAggregator(HiveDefines.MetricId.CHAT_MSGS_DAILY, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should not create an aggregator for invalid metric")
    void invalidMetricTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
           new ActionAggregator(null, null);
        });
    }

    @Test
    @DisplayName("it should not aggregate for invalid data set")
    void invalidDatasetTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final ActionAggregator aggregator = new ActionAggregator(HiveDefines.MetricId.CHAT_MSGS_DAILY, null);
            aggregator.transform(null, new HiveTime(2018,4, 16));
        });
    }

    @Test
    @DisplayName("it should not aggregate for invalid data set")
    void invalidTimeTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final MockEventStore store = new MockEventStore(spark);
            final ActionAggregator aggregator = new ActionAggregator(HiveDefines.MetricId.CHAT_MSGS_DAILY, null);
            aggregator.transform(store.scan(), null);
        });
    }
}
