package io.fizz.chatcommon.infrastructure.messaging.vertx;

import io.fizz.common.Utils;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;

import java.util.*;

public class VertxMqttBrokerImpl implements VertxMqttBroker {
    private final Map<String, MqttEndpoint> endpoints = new HashMap<>();
    private final Map<String, Set<MqttEndpoint>> subscriptions = new HashMap<>();

    public VertxMqttBrokerImpl(final Vertx aVertx) {
        Utils.assertRequiredArgument(aVertx, "invalid vertx instance");

        MqttServer.create(aVertx)
                .endpointHandler(this::onConnected)
                .listen();
    }

    @Override
    public void publish(String aTopicName, String aMessage) {
        final Set<MqttEndpoint> subscribers = subscribers(aTopicName);
        for (MqttEndpoint endpoint: subscribers) {
            endpoint.publish(aTopicName, Buffer.buffer(aMessage), MqttQoS.AT_MOST_ONCE, false, false);
        }
    }

    @Override
    public void subscribe(String aTopicName, String aClientId) {
        final Set<MqttEndpoint> subscribers = subscribers(aTopicName);
        final MqttEndpoint endpoint = endpoints.get(aClientId);

        subscribers.add(endpoint);
    }

    @Override
    public void unsubscribe(String aTopicName, String aClientId) {
        final Set<MqttEndpoint> subscribers = subscribers(aTopicName);
        final MqttEndpoint endpoint = endpoints.get(aClientId);

        subscribers.remove(endpoint);
    }

    private void onConnected(final MqttEndpoint aEndpoint) {
        endpoints.put(aEndpoint.clientIdentifier(), aEndpoint);

        aEndpoint.closeHandler(aVoid -> {
            for (Map.Entry<String, Set<MqttEndpoint>> entry: subscriptions.entrySet()) {
                entry.getValue().remove(aEndpoint);
            }

            endpoints.remove(aEndpoint.clientIdentifier());
        });

        aEndpoint.accept(false);
    }

    private Set<MqttEndpoint> subscribers(final String aTopicName) {
        Set<MqttEndpoint> endpoints = subscriptions.get(aTopicName);
        if (Objects.isNull(endpoints)) {
            endpoints = new HashSet<>();
            subscriptions.put(aTopicName, endpoints);
        }

        return endpoints;
    }
}
