package io.fizz.analytics.jobs.metricsRollup;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.analytics.jobs.metricsRollup.common.*;
import io.fizz.analytics.jobs.metricsRollup.rollup.AbstractMetricsRollupJob;
import io.fizz.analytics.jobs.metricsRollup.rollup.MetricsRollupJobBilling;
import io.fizz.analytics.jobs.metricsRollup.rollup.MetricsRollupJobEvent;
import io.fizz.analytics.jobs.metricsRollup.rollup.MetricsRollupJobKeyword;
import io.fizz.analytics.jobs.metricsRollup.aggregator.AbstractAggregatorTest;
import io.fizz.analytics.jobs.metricsRollup.aggregator.store.event.MockEventStore;
import io.fizz.analytics.jobs.metricsRollup.aggregator.store.event.MockEventStoreDuplicateEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MetricsRollupJobTest extends AbstractAggregatorTest {
    @Test
    @DisplayName("it should run event projections")
    void runEventProjectionsTest() {
        final MockEventStore store = new MockEventStore(spark);
        final AbstractMetricsRollupJob job = new MetricsRollupJobEvent(spark, store, HiveRawEventTableSchema.COL_ID.title(), store::validate,
                new HiveTime(2018, 4, 16),
                new String[]{});
        job.runProjection();
    }

    @Test
    @DisplayName("it should run event projections")
    void runEventProjectionsBillingTest() {
        final MockEventStore store = new MockEventStore(spark);
        final AbstractMetricsRollupJob job = new MetricsRollupJobBilling(spark, store, HiveRawEventTableSchema.COL_ID.title(), store::validate,
                new HiveTime(2018, 4, 16));
        job.runProjection();
    }

    @Test
    @DisplayName("it should run keyword projections")
    void runKeywordProjectionsTest() {
        final MockKeywordsStore store = new MockKeywordsStore(spark);
        final AbstractMetricsRollupJob job = new MetricsRollupJobKeyword(spark, store, store::validate,
                new HiveTime(2018, 4, 16));
        job.runProjection();
    }

    @Test
    @DisplayName("it should run event projections on null unique column")
    void runEventProjectionsNullUniqueColumnTest() {
        final MockEventStore store = new MockEventStore(spark);
        final AbstractMetricsRollupJob job = new MetricsRollupJobEvent(spark, store, null, store::validate,
                new HiveTime(2018, 4, 16),
                new String[]{});
        job.runProjection();
    }

    @Test
    @DisplayName("it should run event projections")
    void runEventProjectionsDuplicateEventsTest() {
        final MockEventStoreDuplicateEvent store = new MockEventStoreDuplicateEvent(spark);
        final AbstractMetricsRollupJob job = new MetricsRollupJobEvent(spark, store, HiveRawEventTableSchema.COL_ID.title(), store::validate,
                new HiveTime(2018, 4, 16),
                new String[]{});
        job.runProjection();
    }
}
