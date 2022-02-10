package io.fizz.gateway.http.verticles;

import io.fizz.gateway.http.services.proxy.eventstream.AbstractEventStreamClient;
import io.fizz.gateway.http.services.proxy.eventstream.EventStreamClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.serviceproxy.ServiceBinder;

import java.util.Objects;

public class MockEventStreamVerticle extends AbstractVerticle {
    public static final String MOCK_EVENT_STREAM_QUEUE = "mock.event.stream.queue";

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        AbstractEventStreamClient service = new EventStreamClient((records, resultHandler) -> {
            if (Objects.isNull(records)) {
                resultHandler.handle(Future.failedFuture("Records Object is Null"));
                return;
            }
            if (records.size() <= 0 || records.size() >= 10) {
                resultHandler.handle(Future.failedFuture("Invalid Records"));
                return;
            }
            resultHandler.handle(Future.succeededFuture(records.size()));
        });
        ServiceBinder binder = new ServiceBinder(vertx);
        binder.setAddress(MOCK_EVENT_STREAM_QUEUE).register(AbstractEventStreamClient.class, service);
        startFuture.complete();
    }
}
