package io.fizz.analytics.jobs.metricsRollupMerge;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.Utils;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.hive.HiveTableDataSource;
import io.fizz.analytics.common.sink.HiveTableSink;
import io.fizz.analytics.common.source.hive.HiveMetricTableSchema;
import io.fizz.analytics.common.source.hive.HiveTempMetricTableSchema;
import io.fizz.analytics.jobs.AbstractJobExecutor;
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
        logger.info("=== Running job to merge metrics");

        final HiveTime time = Utils.previousDay();

        final HiveTableDataSource source = new HiveTableDataSource(spark, new HiveTempMetricTableSchema(), tempDataPath + "/" + HiveTempMetricTableSchema.TABLE_NAME);
        final HiveTableSink sink = new HiveTableSink(spark, new HiveMetricTableSchema(), outputPath + "/" + HiveMetricTableSchema.TABLE_NAME, SaveMode.Overwrite);
        sink.put(source.scanForDay(time));
    }

    public static void main (String[] args) throws Exception {
        final Executor instance = new Executor();

        instance.init();
        instance.execute();

        logger.info("=== exiting application");
    }
}
