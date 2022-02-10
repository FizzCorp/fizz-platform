package io.fizz.analytics.jobs.streamProcessing.stream;

import io.fizz.analytics.jobs.streamProcessing.store.AbstractRecordStore;
import io.fizz.common.ConfigService;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.spark.sql.Row;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class KafkaEventStreamConsumer implements Runnable {
    private static final LoggingService.Log logger = LoggingService.getLogger(KafkaEventStreamConsumer.class);

    private static final int POLL_BATCH_RECORDS_SIZE = 100;
    private static final long POLL_BATCH_TIMEOUT = 1000L;
    private static final long THRESHOLD_TIMEOUT_DUR_MS = 30000;
    private static final long NEW_EVENTS_THRESHOLD = POLL_BATCH_RECORDS_SIZE * 2;

    private static final String KAFKA_SERVERS = ConfigService.instance().getString("gateway.kafka.servers");
    private static final String KAFKA_EVENT_STREAM_TOPIC = ConfigService.instance().getString("gateway.kafka.event.stream.topic");
    private static final String KAFKA_EVENT_STREAM_GROUP = ConfigService.instance().getString("kafka.event.stream.consumer.group");

    private final AbstractRecordStore<Row> store;
    private final CompletableFuture<Void> completable = new CompletableFuture<>();
    private KafkaConsumer<byte[],byte[]> consumer;

    public KafkaEventStreamConsumer(final AbstractRecordStore<Row> aStore) {
        Utils.assertRequiredArgument(aStore, "invalid record store specified.");

        store = aStore;
    }

    public CompletableFuture<Void> completable() {
        return completable;
    }

    private KafkaConsumer<byte[],byte[]> createConsumer() {
        Properties properties=new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVERS);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_EVENT_STREAM_GROUP);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, String.valueOf(POLL_BATCH_RECORDS_SIZE));
        return new KafkaConsumer<byte[],byte[]>(properties);
    }

    public void shutdown() {
        consumer.unsubscribe();
    }

    @Override
    public void run() {
        try {
            consumer = createConsumer();
            consumer.subscribe(Collections.singletonList(KAFKA_EVENT_STREAM_TOPIC));
            long jobStartTime = new Date().getTime();
            long lastProcessedTS = new Date().getTime();
            logger.info("Job Started at : " + jobStartTime);
            int newEventsCount = 0;
            long totalRecordProcessed = 0;
            long oldestEventTS = Long.MAX_VALUE;
            long newestEventTS = Long.MIN_VALUE;
            while (true) {
                final List<String> decodedRecords = new ArrayList<>();
                long startTS = new Date().getTime();
                ConsumerRecords<byte[], byte[]> records = consumer.poll(Duration.ofMillis(POLL_BATCH_TIMEOUT));
                long endTS = new Date().getTime();
                logger.info("@@@@@@ Records Received: " + records.count() + " Time: " + (endTS - startTS) + " Millis");
                totalRecordProcessed += records.count();
                for (ConsumerRecord<byte[], byte[]> record : records) {
                    final byte[] payload = record.value();
                    final long recordTime = record.timestamp();
                    oldestEventTS = Math.min(oldestEventTS, recordTime);
                    newestEventTS = Math.max(newestEventTS, recordTime);

                    final String data = new String(payload, StandardCharsets.UTF_8);
                    decodedRecords.add(data);
                    if (recordTime > jobStartTime) {
                        newEventsCount++;
                        logger.info(">>> post job start record received: " + newEventsCount);
                    }
                }
                store.put(decodedRecords);
                consumer.commitSync();

                if (newEventsCount > NEW_EVENTS_THRESHOLD) {
                    logger.info("JOB COMPLETED: Because of post job messages received: " + newEventsCount);
                    logger.info("Records Processed: " + totalRecordProcessed);
                    logger.info("Oldest Record Time: " + oldestEventTS);
                    logger.info("Newest Record Time: " + newestEventTS);
                    doComplete();
                    break;
                }

                if (new Date().getTime() - lastProcessedTS > THRESHOLD_TIMEOUT_DUR_MS) {
                    logger.info("JOB COMPLETED: because of not enough records received from kafka in " + THRESHOLD_TIMEOUT_DUR_MS + " milliseconds");
                    logger.info("Records Processed: " + totalRecordProcessed);
                    logger.info("Oldest Record Time: " + oldestEventTS);
                    logger.info("Newest Record Time: " + newestEventTS);
                    doComplete();
                    break;
                }

                if (records.count() > 0) {
                    lastProcessedTS = new Date().getTime();
                }
            }
        } catch (Exception ex) {
            completable.completeExceptionally(ex);
        }
    }

    private void doComplete() {
        completable.complete(null);
    }
}