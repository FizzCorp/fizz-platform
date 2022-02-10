package io.fizz.analytics.jobs.metricsRollup;

import io.fizz.analytics.common.Utils;
import io.fizz.analytics.jobs.metricsRollup.rollup.AbstractMetricsRollupJob;
import io.fizz.analytics.jobs.metricsRollup.rollup.MetricsRollupJobBilling;
import io.fizz.analytics.common.HiveTime;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.hive.HiveTableDataSource;
import io.fizz.analytics.jobs.AbstractJobExecutor;
import io.fizz.analytics.common.source.hive.*;
import io.fizz.analytics.common.sink.HiveTableSink;
import io.fizz.analytics.jobs.metricsRollup.rollup.MetricsRollupJobEvent;
import io.fizz.analytics.jobs.metricsRollup.rollup.MetricsRollupJobKeyword;
import org.apache.spark.sql.SaveMode;

/**
 * Aggregates the events on a daily and monthly interval. Results are exported
 * to the metrics hive table. Metrics include:
 * - Sessions Count
 * - Active Users
 * - Total Play Time (session duration)
 * - Characters translated
 * - Sentiments
 */
public class Executor extends AbstractJobExecutor {
    static {
        instance = new Executor();
    }

    private static LoggingService.Log logger = LoggingService.getLogger(Executor.class);

    @Override
    public void execute() throws Exception {
        logger.info("=== Running job to export metrics");

        final HiveTime time = Utils.previousDay();

        final HiveTableSink sink = new HiveTableSink(spark, new HiveTempMetricTableSchema(), tempDataPath + "/" +  HiveTempMetricTableSchema.TABLE_NAME, SaveMode.Append);

        final HiveTableDataSource keywordsStore = new HiveTableDataSource(spark, new HiveKeywordsTableSchema(), dataPath + "/" + HiveKeywordsTableSchema.TABLE_NAME);
        final HiveTableDataSource eventStore = new HiveTableDataSource(spark, new HiveProfileEnrichedEventTableSchema(), dataPath + "/" + HiveProfileEnrichedEventTableSchema.TABLE_NAME);

        final AbstractMetricsRollupJob eventRollupJob = new MetricsRollupJobEvent(spark, eventStore, HiveProfileEnrichedEventTableSchema.COL_ID.title(), sink, time, HiveDefines.MetricSegments.METRIC_ALL_SEGMENTS);
        eventRollupJob.runProjection();

        final AbstractMetricsRollupJob billingRollupJob = new MetricsRollupJobBilling(spark, eventStore, HiveProfileEnrichedEventTableSchema.COL_ID.title(), sink, time);
        billingRollupJob.runProjection();

        final AbstractMetricsRollupJob keywordRollupJob = new MetricsRollupJobKeyword(spark, keywordsStore, sink, time);
        keywordRollupJob.runProjection();
    }

    public static void main (String[] args) throws Exception {
        final Executor instance = new Executor();

        instance.init();
        instance.execute();

        logger.info("=== exiting application");
    }
}
