package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.AbstractSparkTest;
import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.jobs.metricsRollup.aggregator.store.event.*;
import io.fizz.analytics.jobs.metricsRollup.common.MockLogStore;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NewUsersAggregatorTest extends AbstractSparkTest {
    @Test
    @DisplayName("it should aggregate the daily new users count")
    void newUserCountAggregationTest() {
        final MockEventStore store = new MockEventStore(spark);
        final NewUsersAggregator aggregator = new NewUsersAggregator(HiveDefines.MetricId.NEW_USERS_DAILY, null);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the daily new users count segmentValue to Country Code")
    void newUserCountCCAggregationTest() {
        final MockEventStoreSegmentCountryCode store = new MockEventStoreSegmentCountryCode(spark);
        final NewUsersAggregator aggregator = new NewUsersAggregator(HiveDefines.MetricId.NEW_USERS_DAILY, HiveDefines.MetricSegments.SEGMENT_COUNTRY_CODE);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the daily new users count segmentValue to platform")
    void newUserCountPlatformAggregationTest() {
        final MockEventStoreSegmentPlatform store = new MockEventStoreSegmentPlatform(spark);
        final NewUsersAggregator aggregator = new NewUsersAggregator(HiveDefines.MetricId.NEW_USERS_DAILY, HiveDefines.MetricSegments.SEGMENT_PLATFORM);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the daily new users count segmentValue to build")
    void newUserCountBuildAggregationTest() {
        final MockEventStoreSegmentBuild store = new MockEventStoreSegmentBuild(spark);
        final NewUsersAggregator aggregator = new NewUsersAggregator(HiveDefines.MetricId.NEW_USERS_DAILY, HiveDefines.MetricSegments.SEGMENT_BUILD);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the daily new users count segmentValue to age")
    void newUserCountAgeAggregationTest() {
        final MockEventStoreSegmentAge store = new MockEventStoreSegmentAge(spark);
        final NewUsersAggregator aggregator = new NewUsersAggregator(HiveDefines.MetricId.NEW_USERS_DAILY, HiveDefines.MetricSegments.SEGMENT_AGE);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the daily new users count segmentValue to spend")
    void newUserCountSpendAggregationTest() {
        final MockEventStoreSegmentSpend store = new MockEventStoreSegmentSpend(spark);
        final NewUsersAggregator aggregator = new NewUsersAggregator(HiveDefines.MetricId.NEW_USERS_DAILY, HiveDefines.MetricSegments.SEGMENT_SPEND);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the daily new users count segmentValue to custom01")
    void newUserCountCustom01AggregationTest() {
        final MockEventStoreSegmentCustom01 store = new MockEventStoreSegmentCustom01(spark);
        final NewUsersAggregator aggregator = new NewUsersAggregator(HiveDefines.MetricId.NEW_USERS_DAILY, HiveDefines.MetricSegments.SEGMENT_CUSTOM_01);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the daily new users count segmentValue to custom02")
    void newUserCountSegment02AggregationTest() {
        final MockEventStoreSegmentCustom02 store = new MockEventStoreSegmentCustom02(spark);
        final NewUsersAggregator aggregator = new NewUsersAggregator(HiveDefines.MetricId.NEW_USERS_DAILY, HiveDefines.MetricSegments.SEGMENT_CUSTOM_02);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the daily new users count segmentValue to custom03")
    void newUserCountCustom03AggregationTest() {
        final MockEventStoreSegmentCustom03 store = new MockEventStoreSegmentCustom03(spark);
        final NewUsersAggregator aggregator = new NewUsersAggregator(HiveDefines.MetricId.NEW_USERS_DAILY, HiveDefines.MetricSegments.SEGMENT_CUSTOM_03);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018,4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should not aggregate for invalid data set")
    void invalidDatasetTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new NewUsersAggregator(HiveDefines.MetricId.NEW_USERS_DAILY, null)
                    .transform(null, new HiveTime(2018, 4, 16));
        });
    }

    @Test
    @DisplayName("it should not aggregate for invalid time")
    void invalidTimeTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final MockLogStore store = new MockLogStore(spark);
            new NewUsersAggregator(HiveDefines.MetricId.NEW_USERS_DAILY, null)
                    .transform(store.scan(), null);
        });
    }
}
