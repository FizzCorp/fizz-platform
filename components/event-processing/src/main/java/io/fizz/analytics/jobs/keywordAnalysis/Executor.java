package io.fizz.analytics.jobs.keywordAnalysis;

import io.fizz.analytics.common.*;
import io.fizz.analytics.common.hive.HiveTableDataSource;
import io.fizz.analytics.common.projections.Messages2ElasticsearchProjection;
import io.fizz.analytics.jobs.AbstractJobExecutor;
import io.fizz.analytics.common.source.hive.HiveActionTableSchema;
import io.fizz.common.LoggingService;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

/**
 * This job runs sentiment analysis on the actions (chat messages) table
 * and outputs the results to the keywords table.
 */
public class Executor extends AbstractJobExecutor {
    private static LoggingService.Log logger = LoggingService.getLogger(Executor.class);

    @Override
    public void execute() throws Exception {
        logger.info("=== Running job to perform keyword analysis");

        final HiveTime time = Utils.previousDay();

        final HiveActionTableSchema actionSchema = new HiveActionTableSchema();
        final String actionStorePath = String.format("%s/events/%s", dataPath, actionSchema.getTableName());
        final HiveTableDataSource actionStore = new HiveTableDataSource(spark, actionSchema, actionStorePath);

        // export keywords
        final Dataset<Row> actionsDS = actionStore.scan(time.year, time.month, time.dayOfMonth);
        final Dataset<Row> textMessagesDS = actionsDS.filter((FilterFunction<Row>) row -> row.getString(row.fieldIndex(HiveActionTableSchema.COL_ACTION_TYPE)).equals("1"));
        Messages2ElasticsearchProjection.project(textMessagesDS.javaRDD());
    }

    public static void main (String[] args) {
        final Executor instance = new Executor();

        instance.init();
        try {
            instance.execute();
        }
        catch (Exception ex) {
            logger.error(ex.getMessage());
        }

        logger.info("=== exiting application");
    }
}
