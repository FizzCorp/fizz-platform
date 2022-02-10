package io.fizz.analytics.jobs.eventsETL;

import io.fizz.analytics.common.Utils;
import io.fizz.analytics.common.source.hive.*;
import io.fizz.common.ConfigService;
import io.fizz.analytics.common.HiveTime;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.hive.HiveTableDataSource;
import io.fizz.analytics.jobs.AbstractJobExecutor;
import io.fizz.analytics.common.sink.HiveTableSink;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

public class Executor extends AbstractJobExecutor {
    private static final LoggingService.Log logger = LoggingService.getLogger(Executor.class);

    @Override
    public void execute() {
        logger.info("=== Running events ETL job");
        final HiveTime time = Utils.previousDay();
        //final HiveTime time = new HiveTime(2018, 4, 27);

        final int lastDays = ConfigService.instance().getNumber("job.events.etl.last.days").intValue();

        final HiveTableDataSource sessionStore = new HiveTableDataSource(spark, new HiveSessionTableSchema(), dataPath + "/events/session");
        final HiveTableDataSource translationStore = new HiveTableDataSource(spark, new HiveTransTableSchema(), dataPath + "/events/trans");
        final HiveTableDataSource actionStore = new HiveTableDataSource(spark, new HiveActionTableSchema(), dataPath + "/events/action");

        logger.info("scanning table for time: " + time.toString());
        logger.info("scanning table for last days: " + lastDays);

        Dataset<Row> eventsDS = new SessionETLTransformer().transform(sessionStore.scanForLastDaysFrom(lastDays, time), null);
        eventsDS = eventsDS.union(new TranslationETLTransformer().transform(translationStore.scanForLastDaysFrom(lastDays, time), null));
        eventsDS = eventsDS.union(new ActionETLTransformer().transform(actionStore.scanForLastDaysFrom(lastDays, time), null));

        final HiveTableSink sink = new HiveTableSink(spark, new HiveRawEventTableSchema(), outputPath + "/" + HiveRawEventTableSchema.TABLE_NAME, SaveMode.Append);
        sink.put(eventsDS);
    }

    public static void main (String[] args) {
        final Executor instance = new Executor();

        instance.init();
        instance.execute();

        logger.info("=== exiting application");
    }
}
