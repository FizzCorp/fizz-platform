package io.fizz.chat.pubsub.application;

import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.pubsub.domain.topic.TopicMessage;
import io.fizz.chat.pubsub.domain.topic.TopicName;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface AbstractTopicMessagePublisher {
    CompletableFuture<Void> publish(final Set<TopicName> aTopics, final TopicMessage aMessage);
    CompletableFuture<Void> subscribe(final Set<TopicName> aTopics, final SubscriberId aId);
    CompletableFuture<Void> unsubscribe(final Set<TopicName> aTopics, final SubscriberId aId);
}
