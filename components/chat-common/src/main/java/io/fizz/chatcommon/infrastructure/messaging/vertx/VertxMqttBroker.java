package io.fizz.chatcommon.infrastructure.messaging.vertx;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.Vertx;

@ProxyGen
public interface VertxMqttBroker {
    String ADDRESS = "vertx-mqtt-broker";

    static VertxMqttBroker create(final Vertx aVertx) {
        return new VertxMqttBrokerImpl(aVertx);
    }

    static VertxMqttBroker createProxy(final Vertx aVertx, final String aAddress) {
        return new VertxMqttBrokerVertxEBProxy(aVertx, aAddress);
    }

    void publish(final String aTopicName, final String aMessage);
    void subscribe(final String aTopicName, final String aClientId);
    void unsubscribe(final String aTopicName, final String aClientId);
}
