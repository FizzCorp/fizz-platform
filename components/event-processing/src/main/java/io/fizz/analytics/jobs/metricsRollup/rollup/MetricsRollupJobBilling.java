package io.fizz.analytics.jobs.metricsRollup.rollup;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.hive.AbstractTableDataSource;
import io.fizz.analytics.common.sink.AbstractSink;
import io.fizz.analytics.common.source.hive.HiveDefines;
import io.fizz.analytics.jobs.metricsRollup.aggregator.ActiveUserAggregator;
import io.fizz.analytics.jobs.metricsRollup.aggregator.translation.TranslationCharCountAggregator;
import io.fizz.analytics.jobs.metricsRollup.projectors.AggregationProjectorBilling;
import io.fizz.analytics.jobs.metricsRollup.projectors.Grain;
import io.fizz.common.LoggingService;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Objects;


public class MetricsRollupJobBilling implements AbstractMetricsRollupJob {
    private static final LoggingService.Log logger = LoggingService.getLogger(MetricsRollupJobBilling.class);

    private final SparkSession spark;
    private final AggregationProjectorBilling projector;

    public MetricsRollupJobBilling(final SparkSession aSpark,
                                   final AbstractTableDataSource aSource, final String aUniqueColumn,
                                   final AbstractSink<Row> aSink, final HiveTime aTime) {
        if (Objects.isNull(aSpark)) {
            throw new IllegalArgumentException("invalid spark session");
        }
        if (Objects.isNull(aSink)) {
            throw new IllegalArgumentException("invalid data sink");
        }
        spark = aSpark;
        projector = new AggregationProjectorBilling(aSource, aUniqueColumn, aTime);
        projector.addSink(aSink);
    }

    public void runProjection() {
        logger.info("Running billing aggregations on events");

        projector
        .project(Grain.MONTHLY, new TranslationCharCountAggregator(spark, HiveDefines.MetricId.CHARS_TRANSLATED_MONTHLY_BILLING, null));

        projector
        .project(Grain.MID_MONTHLY, new TranslationCharCountAggregator(spark, HiveDefines.MetricId.CHARS_TRANSLATED_MID_MONTHLY_BILLING, null));

        projector
        .project(Grain.MONTHLY, new ActiveUserAggregator(HiveDefines.MetricId.ACTIVE_USERS_MONTHLY_BILLING, null));

        projector
        .project(Grain.MID_MONTHLY, new ActiveUserAggregator(HiveDefines.MetricId.ACTIVE_USERS_MID_MONTHLY_BILLING, null));

        projector.cleanup();
    }
}
