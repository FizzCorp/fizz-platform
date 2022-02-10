package io.fizz.analytics.jobs.metricsRollup.aggregator;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.projections.aggregation.SentimentAggregationProjection;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.jobs.metricsRollup.common.MockKeywordsStore;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KeywordsAggregatorTest extends AbstractAggregatorTest {
    private final SentimentAggregationProjection.MetricGroup group = new SentimentAggregationProjection.MetricGroup(
            HiveDefines.MetricId.SENTIMENT_NEGATIVE_DAILY, HiveDefines.MetricId.SENTIMENT_POSITIVE_DAILY, HiveDefines.MetricId.SENTIMENT_NEUTRAL_DAILY
    );

    @Test
    @DisplayName("it should aggregate sentiment values")
    void aggregationTest() {
        final MockKeywordsStore store = new MockKeywordsStore(spark);
        final Dataset<Row> sentimentCountDS = new KeywordsAggregator(group, spark).transform(store.scan(), new HiveTime(2018, 4, 16));
        store.validate(sentimentCountDS);
    }

    @Test
    @DisplayName("it should not create keywords aggregator for missing metric grouo")
    void invalidMetricGroupTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new KeywordsAggregator(null, spark);
        });
    }

    @Test
    @DisplayName("it should not create keywords aggregator for invalid spark session")
    void invalidSparkSessionTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new KeywordsAggregator(group, null);
        });
    }

    @Test
    @DisplayName("it should not aggregate for invalid data set")
    void invalidDatasetTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new KeywordsAggregator(group, spark).transform(null, new HiveTime(2018, 4, 16));
        });
    }

    @Test
    @DisplayName("it should not aggregate for invalid time")
    void invalidTimeTest() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final MockKeywordsStore store = new MockKeywordsStore(spark);
            new KeywordsAggregator(group, spark).transform(store.scan(), null);
        });
    }
}
