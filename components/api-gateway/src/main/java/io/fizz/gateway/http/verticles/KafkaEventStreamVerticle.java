package io.fizz.gateway.http.verticles;

import io.fizz.gateway.http.services.proxy.eventstream.AbstractEventStreamClient;
import io.fizz.gateway.http.services.proxy.eventstream.EventStreamClient;
import io.fizz.gateway.persistence.KafkaEventStream;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.serviceproxy.ServiceBinder;

public class KafkaEventStreamVerticle extends AbstractVerticle {
    public static final String KAFKA_EVENT_STREAM_QUEUE = "kafka.event.stream.queue";

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        AbstractEventStreamClient service = new EventStreamClient(new KafkaEventStream(vertx));
        ServiceBinder binder = new ServiceBinder(vertx);
        binder.setAddress(KAFKA_EVENT_STREAM_QUEUE).register(AbstractEventStreamClient.class, service);
        startFuture.complete();
    }
}
