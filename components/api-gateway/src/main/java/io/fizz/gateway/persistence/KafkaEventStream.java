package io.fizz.gateway.persistence;

import io.fizz.common.ConfigService;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class KafkaEventStream implements AbstractEventStream {
    protected static final LoggingService.Log logger = LoggingService.getLogger(KafkaEventStream.class);

    private final KafkaProducer<byte[],byte[]> producer;

    private static final String KAFKA_SERVERS = ConfigService.instance().getString("gateway.kafka.servers");
    private static final String KAFKA_EVENT_STREAM_TOPIC = ConfigService.instance().getString("gateway.kafka.event.stream.topic");

    public KafkaEventStream(final Vertx aVertx) {
        producer = createProducer(aVertx);
    }

    private KafkaProducer<byte[],byte[]> createProducer(final Vertx aVertx) {
        Map<String, String> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVERS);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        config.put(ProducerConfig.ACKS_CONFIG, "1");

        return KafkaProducer.create(aVertx, config);
    }

    @Override
    public void put(final List<String> records, final Handler<AsyncResult<Integer>> handler) {
        final List<CompletableFuture<Void>> futures = new ArrayList<>();
        try {
            for (String record : records) {
                futures.add(publish(record));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                    .handle((v, error) -> {
                        try {
                            if (Objects.nonNull(error)) {
                                handler.handle(Future.failedFuture(new Exception("Failed to put record in kafka due: "
                                        + error.getMessage())));
                                logger.error(error.getMessage());
                                return null;
                            }
                            handler.handle(Future.succeededFuture(records.size()));
                        } catch (Exception ex) {
                            logger.error(ex.getMessage());
                        }
                        return null;
                    });
        } catch (Exception ex) {
            handler.handle(Future.failedFuture(ex));
        }
    }

    private CompletableFuture<Void> publish(final String aRecord) {
        try {
            final byte[] byteData = aRecord.getBytes(StandardCharsets.UTF_8);
            final KafkaProducerRecord<byte[],byte[]> record = KafkaProducerRecord.create(KAFKA_EVENT_STREAM_TOPIC, byteData);

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
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }
}
