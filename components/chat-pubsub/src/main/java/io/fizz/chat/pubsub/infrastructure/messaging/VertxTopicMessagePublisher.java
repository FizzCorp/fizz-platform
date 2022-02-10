package io.fizz.chat.pubsub.infrastructure.messaging;

import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.pubsub.domain.topic.TopicMessage;
import io.fizz.chat.pubsub.domain.topic.TopicName;
import io.fizz.chat.pubsub.infrastructure.serde.AbstractMessageSerde;
import io.fizz.chat.pubsub.infrastructure.serde.JsonTopicMessageSerde;
import io.fizz.chatcommon.infrastructure.messaging.vertx.VertxMqttBroker;
import io.fizz.common.Utils;
import io.vertx.core.Vertx;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class VertxTopicMessagePublisher implements AbstractTopicMessagePublisher {
    private static final char TOPIC_DELIMITER = '/';
    private final VertxMqttBroker broker;
    private final AbstractMessageSerde serde = new JsonTopicMessageSerde();

    public VertxTopicMessagePublisher(final Vertx aVertx) {
        broker = VertxMqttBroker.createProxy(aVertx, VertxMqttBroker.ADDRESS);
    }

    @Override
    public CompletableFuture<Void> publish(final Set<TopicName> aTopics, final TopicMessage aMessage) {
        Utils.assertRequiredArgument(aTopics, "invalid topics");
        Utils.assertRequiredArgument(aMessage, "invalid message");

        for (final TopicName topic: aTopics) {
            broker.publish(topic.value(TOPIC_DELIMITER), serde.serialize(aMessage));
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> subscribe(final Set<TopicName> aTopics, final SubscriberId aId) {
        Utils.assertRequiredArgument(aTopics, "invalid topics");
        Utils.assertRequiredArgument(aId, "invalid subscriber id");

        for (final TopicName topic: aTopics) {
            broker.subscribe(topic.value(TOPIC_DELIMITER), aId.value());
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> unsubscribe(final Set<TopicName> aTopics, final SubscriberId aId) {
        Utils.assertRequiredArgument(aTopics, "invalid topics");
        Utils.assertRequiredArgument(aId, "invalid subscriber id");

        for (final TopicName topic: aTopics) {
            broker.unsubscribe(topic.value(TOPIC_DELIMITER), aId.value());
        }

        return CompletableFuture.completedFuture(null);
    }
}
