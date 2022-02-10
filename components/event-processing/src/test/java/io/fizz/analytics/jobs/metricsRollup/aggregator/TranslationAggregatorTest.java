package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.jobs.metricsRollup.aggregator.store.event.*;
import io.fizz.analytics.jobs.metricsRollup.aggregator.translation.TranslationCharCountAggregator;
import io.fizz.analytics.jobs.metricsRollup.aggregator.translation.TranslationCountAggregator;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TranslationAggregatorTest extends AbstractAggregatorTest {
    @Test
    @DisplayName("it should aggregate the translated characters")
    void translationCountAggregateTest() {
        final MockEventStore store = new MockEventStore(spark);
        final TranslationCountAggregator aggregator = new TranslationCountAggregator(spark, HiveDefines.MetricId.TRANSLATION_COUNT_DAILY, null);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018, 4, 16));
        store.validate(metricsDS);
    }

    @Test
    @DisplayName("it should aggregate the translated characters")
    void translationCharCountAggregateTest() {
        final MockEventStore store = new MockEventStore(spark);
        final TranslationCharCountAggregator aggregator = new TranslationCharCountAggregator(spark, HiveDefines.MetricId.CHARS_TRANSLATED_DAILY, null);
        final Dataset<Row> metricsDS = aggregator.transform(store.scan(), new HiveTime(2018, 4, 16));
        store.validate(metricsDS);
    }
}
