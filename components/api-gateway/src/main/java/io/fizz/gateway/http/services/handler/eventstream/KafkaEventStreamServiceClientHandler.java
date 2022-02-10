package io.fizz.gateway.http.services.handler.eventstream;

import com.google.gson.Gson;
import io.fizz.common.LoggingService;
import io.fizz.common.domain.events.AbstractDomainEvent;
import io.fizz.gateway.http.services.proxy.eventstream.AbstractEventStreamClient;
import io.fizz.gateway.http.verticles.KafkaEventStreamVerticle;
import io.vertx.core.Vertx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class KafkaEventStreamServiceClientHandler implements AbstractEventStreamClientHandler {
    private final static LoggingService.Log logger = LoggingService.getLogger(KafkaEventStreamServiceClientHandler.class);

    private final AbstractEventStreamClient eventStreamService;

    public KafkaEventStreamServiceClientHandler(final Vertx aVertx) {
        if (Objects.isNull(aVertx)) {
            throw new IllegalArgumentException("invalid vertx instance specified.");
        }
        eventStreamService = getEventStreamService(aVertx);
    }

    AbstractEventStreamClient getEventStreamService(Vertx aVertx) {
        return AbstractEventStreamClient.createProxy(aVertx, KafkaEventStreamVerticle.KAFKA_EVENT_STREAM_QUEUE);
    }

    @Override
    public CompletableFuture<Integer> put(final List<AbstractDomainEvent> aEvents) throws IllegalArgumentException {
        final CompletableFuture<Integer> future = new CompletableFuture<>();
        if (Objects.isNull(aEvents) || aEvents.size() <= 0) {
            throw new IllegalArgumentException("invalid_event_data");
        }

        logger.debug(String.format("Putting %d records in kafka stream", aEvents.size()));

        final List<String> records = new ArrayList<>();
        final Gson serde = new Gson();
        for (AbstractDomainEvent event: aEvents) {
            records.add(serde.toJson(event));
        }

        eventStreamService.put(records, ar -> {
            if (ar.failed()) {
                logger.error(ar.cause().getMessage());
                future.completeExceptionally(ar.cause());
                return;
            }
            future.complete(ar.result());
        });

        return future;
    }
}
