package io.fizz.chatcommon.infrastructure.messaging.kafka;

import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.AbstractEventListener;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.chatcommon.infrastructure.messaging.AbstractBaseEventPublisher;
import io.fizz.chatcommon.infrastructure.serde.AbstractEventSerde;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.kafka.client.consumer.KafkaConsumerRecords;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class KafkaEventPublisher extends AbstractBaseEventPublisher {
    private static final LoggingService.Log log = LoggingService.getLogger(KafkaEventPublisher.class);

    private final KafkaProducer<byte[],byte[]> producer;
    private final KafkaConsumer<byte[],byte[]> consumer;
    private final String topic;
    private final AbstractEventSerde serde;
    private final Vertx vertx;

    public KafkaEventPublisher(final Vertx aVertx,
                               final AbstractEventSerde aSerde,
                               final DomainEventType[] aListenedEvents,
                               final String aBootstrapServers,
                               final String aTopic,
                               final String aGroupId) {
        super(aListenedEvents);

        Utils.assertRequiredArgument(aVertx, "invalid vertx instance");
        Utils.assertRequiredArgument(aSerde, "invalid event serde");
        Utils.assertRequiredArgument(aBootstrapServers, "invalid kafka bootstrap servers");
        Utils.assertRequiredArgument(aTopic, "invalid kafka topic");
        Utils.assertRequiredArgument(aGroupId, "invalid consumer group");

        vertx = aVertx;
        topic = aTopic;
        serde = aSerde;
        producer = createProducer(aVertx, aBootstrapServers);
        consumer = createConsumer(aVertx, aBootstrapServers, aGroupId);
        consumer.subscribe(topic, this::onSubscribe);
        consumer.handler(this::onRecord);
    }

    @Override
    public CompletableFuture<Void> publish(final AbstractDomainEvent aEvent) {
        try {
            final byte[] key =
                    Objects.nonNull(aEvent.streamId()) ? aEvent.streamId().getBytes(StandardCharsets.UTF_8) : null;
            final byte[] payload = serde.serialize(aEvent).getBytes(StandardCharsets.UTF_8);
            final KafkaProducerRecord<byte[],byte[]> record = AvroKafkaRecordSerde.create(topic, key, payload);

            final CompletableFuture<Void> published = new CompletableFuture<>();
            producer.write(record, aResult -> {
                if (aResult.succeeded()) {
                    published.complete(null);
                } else {
                    published.completeExceptionally(aResult.cause());
                }
            });

            return published;
        }
        catch (IOException ex) {
            return Utils.failedFuture(ex);
        }
    }

    private void onSubscribe(AsyncResult<Void> aResult) {
        if (aResult.failed()) {
            log.fatal(aResult.cause().getMessage());
            return;
        }

        vertx.setPeriodic(100, timeId -> consumer.poll(50L, this::onPoll));
    }

    private void onPoll(final AsyncResult<KafkaConsumerRecords<byte[],byte[]>> aResult) {
        if (aResult.failed()) {
            log.error(aResult.cause().getMessage());
            return;
        }

        consumer.pause();

        final KafkaConsumerRecords<byte[],byte[]> records = aResult.result();
        final List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int ri = 0; ri < records.size(); ri++) {
            final KafkaConsumerRecord<byte[],byte[]> record = records.recordAt(ri);
            futures.add(onRecord(record));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((aVoid, aError) -> {
                    if (Objects.nonNull(aError)) {
                        log.fatal(aError.getMessage());
                    }
                    else {
                        consumer.resume();
                        consumer.commit();
                    }
                });
    }

    private CompletableFuture<Void> onRecord(final KafkaConsumerRecord<byte[],byte[]> aRecord) {
        try {
            final byte[] payload = AvroKafkaRecordSerde.payload(aRecord);
            final AbstractDomainEvent event = serde.deserialize(new String(payload, StandardCharsets.UTF_8));
            final List<AbstractEventListener> listeners = createOrFindListeners(event.type());
            final List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (final AbstractEventListener listener : listeners) {
                futures.add(listener.handleEvent(event));
            }

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }
        catch (IOException ex) {
            return Utils.failedFuture(ex);
        }
    }

    private KafkaProducer<byte[],byte[]> createProducer(final Vertx aVertx, final String aBootstrapServers) {
        Map<String, String> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, aBootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        config.put(ProducerConfig.ACKS_CONFIG, "1");

        return KafkaProducer.create(aVertx, config);
    }

    private KafkaConsumer<byte[],byte[]> createConsumer(final Vertx aVertx,
                                                         final String aBootstrapServers,
                                                         final String aGroupId) {
        Map<String, String> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, aBootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, aGroupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        return KafkaConsumer.create(aVertx, config);
    }
}
