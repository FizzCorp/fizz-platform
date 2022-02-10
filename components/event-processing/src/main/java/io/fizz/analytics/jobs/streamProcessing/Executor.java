package io.fizz.analytics.jobs.streamProcessing;

import io.fizz.analytics.jobs.streamProcessing.stream.KafkaEventStreamConsumer;
import io.fizz.common.LoggingService;
import io.fizz.analytics.common.sink.HiveTableSink;
import io.fizz.analytics.common.source.hive.HiveRawEventTableSchema;
import io.fizz.analytics.jobs.AbstractJobExecutor;
import io.fizz.analytics.jobs.streamProcessing.store.InMemoryRecordStore;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import java.util.concurrent.CompletableFuture;

public class Executor extends AbstractJobExecutor {
    private static final LoggingService.Log logger = LoggingService.getLogger(Executor.class);

    public static void main (String[] args) {
        final Executor instance = new Executor();

        instance.init();
        try {
            instance.execute();
        }
        catch (Exception ex) {
            logger.fatal("Streaming job failed with exception: " + ex.getMessage());
        }

        logger.info("=== exiting application");
    }

    @Override
    public void execute() throws Exception {
        logger.info("executing job to process event stream.");

        final InMemoryRecordStore store = new InMemoryRecordStore(spark);

        KafkaEventStreamConsumer consumer = new KafkaEventStreamConsumer(store);
        final CompletableFuture<Void> processCompleted = consumer.completable();

        logger.info("starting worker to process stream records");
        new Thread(consumer).start();
        processCompleted.get();

        logger.info("persisting records processed from kafka");
        final Dataset<Row> events = store.createDataset();
        final HiveTableSink rawEventsSink = new HiveTableSink(spark, new HiveRawEventTableSchema(), outputPath + "/" + HiveRawEventTableSchema.TABLE_NAME, SaveMode.Append);
        rawEventsSink.put(events);

        logger.info("shutting down worker");
        consumer.shutdown();
        logger.info("Shutdown event processing");
    }
}
