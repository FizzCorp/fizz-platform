package io.fizz.chat.application.topic;

import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chat.pubsub.domain.topic.TopicMessage;
import io.fizz.chat.pubsub.domain.topic.TopicName;
import io.fizz.chatcommon.domain.events.DomainEventBus;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;

import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class TopicApplicationService {
    private final AbstractTopicMessagePublisher messagePublisher;

    public TopicApplicationService(final DomainEventBus aEventBus,
                                   final AbstractTopicMessagePublisher aMessagePublisher) {
        if (Objects.isNull(aEventBus)) {
            throw new IllegalArgumentException("invalid event bus specified.");
        }
        if (Objects.isNull(aMessagePublisher)) {
            throw new IllegalArgumentException("invalid message publisher specified.");
        }

        messagePublisher = aMessagePublisher;
    }

    public AbstractTopicMessagePublisher getMessagePublisher() {
        return messagePublisher;
    }

    public CompletableFuture<Void> publish(final ApplicationId aAppId,
                                           final TopicId aTopicId,
                                           final TopicMessage aMessage) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aAppId, "invalid_app_id");
            Utils.assertRequiredArgument(aTopicId, "invalid_topic_id");

            final TopicName topic = new TopicName().append(aAppId.value()).append(aTopicId.value());

            return messagePublisher.publish(Collections.singleton(topic), aMessage);
        });
    }

    public CompletableFuture<Void> publish(final String aAppId,
                                           final String aChannelId,
                                           final Long aMessageId,
                                           final String aType,
                                           final String aFrom,
                                           final String aData) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            final TopicId topicId = new TopicId(aChannelId);
            final TopicName topic = new TopicName().append(appId.value()).append(topicId.value());

            final TopicMessage message = new TopicMessage(
                aMessageId,
                aType,
                aFrom,
                aData,
                new Date()
            );

            return messagePublisher.publish(Collections.singleton(topic), message);
        }
        catch (DomainErrorException ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<Void> subscribe(final String aAppId,
                                             final String aTopicId,
                                             final String aSubscriberId) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            final TopicId topicId = new TopicId(aTopicId);
            final SubscriberId subscriber = new SubscriberId(aSubscriberId);
            final TopicName topic = new TopicName().append(appId.value()).append(topicId.value());

            return messagePublisher.subscribe(Collections.singleton(topic), subscriber);
        }
        catch (DomainErrorException ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<Void> unsubscribe(final String aAppId,
                                               final String aTopicId,
                                               final String aSubscriberId) {
        return Utils.async(() -> {
            try {
                final ApplicationId appId = new ApplicationId(aAppId);
                final TopicId topicId = new TopicId(aTopicId);
                final SubscriberId subscriberId = new SubscriberId(aSubscriberId);
                final TopicName topic = new TopicName().append(appId.value()).append(topicId.value());

                return messagePublisher.unsubscribe(Collections.singleton(topic), subscriberId);
            }
            catch (DomainErrorException ex) {
                return Utils.failedFuture(ex);
            }
        });
    }
}
