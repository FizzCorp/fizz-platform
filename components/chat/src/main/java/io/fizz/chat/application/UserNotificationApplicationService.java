package io.fizz.chat.application;

import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.pubsub.domain.topic.TopicMessage;
import io.fizz.chat.pubsub.domain.topic.TopicName;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class UserNotificationApplicationService {
    static final String NAMESPACE = "notify";
    private final AbstractTopicMessagePublisher messagePublisher;

    public UserNotificationApplicationService(final AbstractTopicMessagePublisher aMessagePublisher) {
        Utils.assertRequiredArgument(aMessagePublisher, "invalid message publisher");

        this.messagePublisher = aMessagePublisher;
    }

    public CompletableFuture<Void> subscribe(final ApplicationId aAppId,
                                             final UserId aUserId,
                                             final SubscriberId aSubscriberId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aSubscriberId, "invalid_subscriber_id");

            final TopicName topic = build(aAppId, aUserId);

            return messagePublisher.subscribe(Collections.singleton(topic), aSubscriberId);
        });
    }

    public CompletableFuture<Void> unsubscribe(final ApplicationId aAppId,
                                               final UserId aUserId,
                                               final SubscriberId aSubscriberId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aSubscriberId, "invalid_subscriber_id");

            final TopicName topic = build(aAppId, aUserId);

            return messagePublisher.unsubscribe(Collections.singleton(topic), aSubscriberId);
        });
    }

    public CompletableFuture<Void> publish(final ApplicationId aAppId,
                                           final Set<UserId> aUserIds,
                                           final TopicMessage aMessage) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aMessage, "invalid_message");

            final Set<TopicName> topics = build(aAppId, aUserIds);

            return messagePublisher.publish(topics, aMessage);
        });
    }

    public Set<TopicName> build(final ApplicationId aAppId, final Set<UserId> aUserIds) {
        Utils.assertRequiredArgument(aUserIds, "invalid_user_ids");

        final Set<TopicName> topics = new HashSet<>();
        for (final UserId id: aUserIds) {
            topics.add(build(aAppId, id));
        }
        return topics;
    }

    public TopicName build(final ApplicationId aAppId, final UserId aUserId) {
        Utils.assertRequiredArgument(aAppId, "invalid_app_id");
        Utils.assertRequiredArgument(aUserId, "invalid_user_id");

        return new TopicName().append(NAMESPACE).append(aAppId.value()).append(aUserId.value());
    }
}
