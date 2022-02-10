package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.jobs.metricsRollup.aggregator.store.event.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SentimentAggregatorTest extends AbstractAggregatorTest {
    @Test
    @DisplayName("it should aggregate the sentiment metrics")
    void sentimentMetricsAggregateTest() {
        final MockEventStore store = new MockEventStore(spark);
        final SentimentAggregator aggregator = new SentimentAggregator(spark, HiveDefines.MetricId.SENTIMENT_DAILY, null);
        final Dataset<Row> metricsDS =  aggregator.transform(store.scan(), new HiveTime(2018, 4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the sentiment metrics segmentValue to Country Code")
    void sentimentMetricsCCAggregationTest() {
        final MockEventStoreSegmentCountryCode store = new MockEventStoreSegmentCountryCode(spark);
        final SentimentAggregator aggregator = new SentimentAggregator(spark, HiveDefines.MetricId.SENTIMENT_DAILY, HiveDefines.MetricSegments.SEGMENT_COUNTRY_CODE);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the sentiment metrics segmentValue to platform")
    void sentimentMetricsPlatformAggregationTest() {
        final MockEventStoreSegmentPlatform store = new MockEventStoreSegmentPlatform(spark);
        final SentimentAggregator aggregator = new SentimentAggregator(spark, HiveDefines.MetricId.SENTIMENT_DAILY, HiveDefines.MetricSegments.SEGMENT_PLATFORM);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the sentiment metrics segmentValue to build")
    void sentimentMetricsBuildAggregationTest() {
        final MockEventStoreSegmentBuild store = new MockEventStoreSegmentBuild(spark);
        final SentimentAggregator aggregator = new SentimentAggregator(spark, HiveDefines.MetricId.SENTIMENT_DAILY, HiveDefines.MetricSegments.SEGMENT_BUILD);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the sentiment metrics segmentValue to age")
    void sentimentMetricsAgeAggregationTest() {
        final MockEventStoreSegmentAge store = new MockEventStoreSegmentAge(spark);
        final SentimentAggregator aggregator = new SentimentAggregator(spark, HiveDefines.MetricId.SENTIMENT_DAILY, HiveDefines.MetricSegments.SEGMENT_AGE);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the sentiment metrics segmentValue to spend")
    void sentimentMetricsSpendAggregationTest() {
        final MockEventStoreSegmentSpend store = new MockEventStoreSegmentSpend(spark);
        final SentimentAggregator aggregator = new SentimentAggregator(spark, HiveDefines.MetricId.SENTIMENT_DAILY, HiveDefines.MetricSegments.SEGMENT_SPEND);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the sentiment metrics segmentValue to custom01")
    void sentimentMetricsCustom01AggregationTest() {
        final MockEventStoreSegmentCustom01 store = new MockEventStoreSegmentCustom01(spark);
        final SentimentAggregator aggregator = new SentimentAggregator(spark, HiveDefines.MetricId.SENTIMENT_DAILY, HiveDefines.MetricSegments.SEGMENT_CUSTOM_01);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the sentiment metrics segmentValue to custom02")
    void sentimentMetricsSegment02AggregationTest() {
        final MockEventStoreSegmentCustom02 store = new MockEventStoreSegmentCustom02(spark);
        final SentimentAggregator aggregator = new SentimentAggregator(spark, HiveDefines.MetricId.SENTIMENT_DAILY, HiveDefines.MetricSegments.SEGMENT_CUSTOM_02);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the sentiment metrics segmentValue to custom03")
    void sentimentMetricsCustom03AggregationTest() {
        final MockEventStoreSegmentCustom03 store = new MockEventStoreSegmentCustom03(spark);
        final SentimentAggregator aggregator = new SentimentAggregator(spark, HiveDefines.MetricId.SENTIMENT_DAILY, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should not run transform for invalid data set")
    void invalidTransformDataSetTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final SentimentAggregator aggregator = new SentimentAggregator(spark, HiveDefines.MetricId.SENTIMENT_DAILY, null);
            aggregator.transform(null, new HiveTime(2018, 4, 16));
        });
    }

    @Test
    @DisplayName("it should not run transform for invalid time")
    void invalidTransformTimeTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final SentimentAggregator aggregator = new SentimentAggregator(spark, HiveDefines.MetricId.SENTIMENT_DAILY, null);
            aggregator.transform(new MockEventStore(spark).scan(), null);
        });
    }
}
