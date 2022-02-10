package io.fizz.analytics.jobs.metricsRollup.aggregator.translation;

import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.jobs.metricsRollup.aggregator.AggregateType;
import org.apache.spark.sql.*;

public class TranslationCountAggregator extends AbstractTranslationAggregator {
    public TranslationCountAggregator(final SparkSession aSpark, final HiveDefines.MetricId aTranslationsMetricId, String aSegment) {
        super(aSpark, aTranslationsMetricId, aSegment);
    }
}
