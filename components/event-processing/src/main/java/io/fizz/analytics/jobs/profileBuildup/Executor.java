package io.fizz.analytics.jobs.profileBuildup;

import io.fizz.analytics.common.HiveTime;
import io.fizz.analytics.common.Utils;
import io.fizz.analytics.common.source.hive.HiveSentimentEventTableSchema;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.hive.HiveTableDataSource;
import io.fizz.analytics.common.sink.HiveTableSink;
import io.fizz.analytics.common.source.hive.HiveProfileEnrichedEventTableSchema;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.analytics.jobs.AbstractJobExecutor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

public class Executor extends AbstractJobExecutor {
    private static final LoggingService.Log logger = LoggingService.getLogger(Executor.class);

    public static void main (String[] args) {
        final Executor instance = new Executor();

        instance.init();
        try {
            instance.execute();
        }
        catch (Exception ex) {
            logger.fatal("Profile Enrich job failed with exception: " + ex.getMessage());
        }

        logger.info("=== exiting application");
    }

    @Override
    public void execute() throws Exception {
        logger.info("executing job to enrich user profiles.");

        final HiveTime time = Utils.previousDay();

        final HiveTableDataSource eventsStore = new HiveTableDataSource(spark, new HiveSentimentEventTableSchema(), dataPath + "/" + HiveSentimentEventTableSchema.TABLE_NAME);
        final Dataset<Row> events = eventsStore.scanForLastDaysFrom(0, time);

        final ProfileSegmentTransformer transformer = new ProfileSegmentTransformer();
        final Dataset<Row> segmentedEvents = transformer.transform(events, time);
        final HiveTableSink enrichedEventsSink = new HiveTableSink(spark, new HiveProfileEnrichedEventTableSchema(), outputPath + "/" + HiveProfileEnrichedEventTableSchema.TABLE_NAME, SaveMode.Overwrite);
        enrichedEventsSink.put(segmentedEvents);

        logger.info("Shutdown profile enrich processing");
    }
}

