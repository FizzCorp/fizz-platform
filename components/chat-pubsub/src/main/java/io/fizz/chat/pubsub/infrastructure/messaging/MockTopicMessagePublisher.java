package io.fizz.chat.pubsub.infrastructure.messaging;

import io.fizz.common.Utils;
import io.fizz.chat.pubsub.domain.topic.TopicName;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.pubsub.domain.topic.TopicMessage;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MockTopicMessagePublisher implements AbstractTopicMessagePublisher {
    @Override
    public CompletableFuture<Void> publish(final Set<TopicName> aTopics, final TopicMessage aMessage) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aTopics, "invalid_topics");
            Utils.assertRequiredArgument(aMessage, "invalid_message");

            return CompletableFuture.completedFuture(null);
        });
    }

    @Override
    public CompletableFuture<Void> subscribe(final Set<TopicName> aTopics, final SubscriberId aId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aTopics, "invalid_topics");
            Utils.assertRequiredArgument(aId, "invalid_subscriber_id");

            return CompletableFuture.completedFuture(null);
        });
    }

    @Override
    public CompletableFuture<Void> unsubscribe(final Set<TopicName> aTopics, final SubscriberId aId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aTopics, "invalid_topics");
            Utils.assertRequiredArgument(aId, "invalid_subscriber_id");

            return CompletableFuture.completedFuture(null);
        });
    }
}
