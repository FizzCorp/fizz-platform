package io.fizz.gateway.http.services.handler.eventstream;

import io.fizz.gateway.http.services.proxy.eventstream.AbstractEventStreamClient;
import io.fizz.gateway.http.verticles.MockEventStreamVerticle;
import io.vertx.core.Vertx;

public class MockEventStreamServiceClientHandler extends KafkaEventStreamServiceClientHandler {
    public MockEventStreamServiceClientHandler(final Vertx aVertx) {
        super(aVertx);
    }

    @Override
    AbstractEventStreamClient getEventStreamService(Vertx aVertx) {
        return AbstractEventStreamClient.createProxy(aVertx, MockEventStreamVerticle.MOCK_EVENT_STREAM_QUEUE);
    }
}
