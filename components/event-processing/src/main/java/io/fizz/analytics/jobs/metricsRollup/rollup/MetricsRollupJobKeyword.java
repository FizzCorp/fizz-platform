package io.fizz.analytics.jobs.metricsRollup.rollup;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.hive.AbstractTableDataSource;
import io.fizz.analytics.common.sink.AbstractSink;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.jobs.metricsRollup.projectors.AggregationProjector;
import io.fizz.analytics.jobs.metricsRollup.projectors.Grain;
import io.fizz.analytics.jobs.metricsRollup.aggregator.KeywordsAggregator;
import io.fizz.common.LoggingService;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Objects;

public class MetricsRollupJobKeyword implements AbstractMetricsRollupJob {
    private static final LoggingService.Log logger = LoggingService.getLogger(MetricsRollupJobKeyword.class);

    private final SparkSession spark;
    private final AggregationProjector projector;

    public MetricsRollupJobKeyword(final SparkSession aSpark,
                                   final AbstractTableDataSource aSource,
                                   final AbstractSink<Row> aSink, final HiveTime aTime) {
        if (Objects.isNull(aSpark)) {
            throw new IllegalArgumentException("invalid spark session");
        }
        if (Objects.isNull(aSink)) {
            throw new IllegalArgumentException("invalid data sink");
        }
        spark = aSpark;
        projector = new AggregationProjector(aSource, null, aTime);
        projector.addSink(aSink);
    }

    public void runProjection() {
        logger.info("Running aggregations on keyword data.");

        projector
        .project(Grain.DAILY, new KeywordsAggregator(new KeywordsAggregator.MetricGroup(
                HiveDefines.MetricId.SENTIMENT_NEGATIVE_DAILY,
                HiveDefines.MetricId.SENTIMENT_POSITIVE_DAILY,
                HiveDefines.MetricId.SENTIMENT_NEUTRAL_DAILY
        ), spark));

        projector
        .project(Grain.MONTHLY, new KeywordsAggregator(new KeywordsAggregator.MetricGroup(
                HiveDefines.MetricId.SENTIMENT_NEGATIVE_MONTHLY,
                HiveDefines.MetricId.SENTIMENT_POSITIVE_MONTHLY,
                HiveDefines.MetricId.SENTIMENT_NEUTRAL_MONTHLY
        ), spark));
    }
}
