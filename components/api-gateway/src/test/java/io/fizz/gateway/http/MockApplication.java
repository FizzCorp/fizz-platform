package io.fizz.gateway.http;

import io.fizz.chatcommon.infrastructure.messaging.vertx.VertxMqttBroker;
import io.fizz.gateway.http.services.handler.eventstream.MockEventStreamServiceClientHandler;
import io.fizz.gateway.http.verticles.HTTPVerticle;
import io.fizz.gateway.http.verticles.MockEventStreamVerticle;
import io.fizz.gateway.http.verticles.MockHttpVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;

public class MockApplication extends Application {
    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(MockApplication.class.getName());
    }

    public MockApplication() {
    }

    @Override
    protected Future<Void> buildPipeline()
    {
        return startMqttBroker()
                .compose(v -> super.buildPipeline());
    }

    private Future<String> startMqttBroker() {
        VertxMqttBroker broker = VertxMqttBroker.create(vertx);
        new ServiceBinder(vertx)
                .setAddress(VertxMqttBroker.ADDRESS)
                .register(VertxMqttBroker.class, broker);
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> createEventStreamHandler() {
        eventStreamHandler = new MockEventStreamServiceClientHandler(vertx);
        return Future.succeededFuture();
    }

    @Override
    protected Future<String> startHBaseClient() {
        return Future.succeededFuture();
    }

    @Override
    protected String eventStreamService() {
        return MockEventStreamVerticle.class.getName();
    }

    @Override
    protected Future<Void> openRPCServer() {
        return Future.succeededFuture();
    }

    @Override
    protected HTTPVerticle buildHTTPService() {
        return new MockHttpVerticle(
                API_PORT,
                API_PORT_INTERNAL,
                eventStreamHandler,
                rpcServiceHost,
                PROXY_PORT
        );
    }

    @Override
    protected int servicesCount() {
        return 1;
    }
}
