package io.fizz.chat.user.application;

import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.pubsub.domain.topic.TopicMessage;
import io.fizz.chat.pubsub.domain.topic.TopicName;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;
import io.fizz.chat.user.domain.User;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ProfileNotificationService {
    private static final String EVENT_TYPE_PRESENCE_UPDATE = "USRPU";
    private static final String EVENT_TYPE_USER_UPDATE = "USRU";
    private static final String NAMESPACE = "profile";

    private final AbstractTopicMessagePublisher publisher;

    public ProfileNotificationService(final AbstractTopicMessagePublisher aPublisher) {
        this.publisher = aPublisher;
    }

    public CompletableFuture<Void> publishOnlineStatus(final ApplicationId aAppId,
                                                final UserId aUserId,
                                                final boolean isOnline) {
        try {
            Utils.assertRequiredArgument(aAppId, "invalid_app_id");
            Utils.assertRequiredArgument(aUserId, "invalid_user_id");

            String PRESENCE_PAYLOAD = "{\"is_online\":%s}";
            final String payload = String.format(PRESENCE_PAYLOAD, isOnline);
            final TopicMessage message =
                    new TopicMessage(0L, EVENT_TYPE_PRESENCE_UPDATE, aUserId.value(), payload, new Date());

            final TopicName topic = topic(aAppId, aUserId);
            return publisher.publish(Collections.singleton(topic), message);
        } catch (IllegalArgumentException ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<Void> publishUserUpdated(final User aUser) {
        try {
            Utils.assertRequiredArgument(aUser, "invalid_user");

            String PROFILE_PAYLOAD = "{\"nick\":\"%s\",\"status_message\":\"%s\",\"profile_url\":\"%s\"}";

            final String payload = String.format(PROFILE_PAYLOAD,
                                                Objects.nonNull(aUser.nick()) ? aUser.nick().value() : "",
                                                Objects.nonNull(aUser.statusMessage()) ? aUser.statusMessage().value(): "",
                                                Objects.nonNull(aUser.profileUrl()) ? aUser.profileUrl().value() : "");
            final TopicMessage message =
                    new TopicMessage(0L, EVENT_TYPE_USER_UPDATE, aUser.userId().value(), payload, new Date());

            final TopicName topic = topic(aUser.appId(), aUser.userId());
            return publisher.publish(Collections.singleton(topic), message);
        } catch (IllegalArgumentException ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<Void> subscribe(final ApplicationId aAppId, final Set<UserId> aUserIds, final SubscriberId aSubscriberId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aAppId, "invalid_app_id");
            Utils.assertRequiredArgument(aUserIds, "invalid_user_ids");
            Utils.assertRequiredArgument(aSubscriberId, "invalid_subscriber_id");

            if (aUserIds.size() == 0) {
                return CompletableFuture.completedFuture(null);
            }

            Set<TopicName> topics = new HashSet<>(aUserIds.size());
            for (UserId id : aUserIds) {
                topics.add(topic(aAppId, id));
            }

            return publisher.subscribe(topics, aSubscriberId);
        });
    }

    public CompletableFuture<Void> unsubscribe(final ApplicationId aAppId, final Set<UserId> aUserIds, final SubscriberId aSubscriberId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aAppId, "invalid_app_id");
            Utils.assertRequiredArgument(aUserIds, "invalid_user_ids");
            Utils.assertRequiredArgument(aSubscriberId, "invalid_subscriber_id");

            if (aUserIds.size() == 0) {
                return CompletableFuture.completedFuture(null);
            }

            Set<TopicName> topics = new HashSet<>(aUserIds.size());
            for (UserId id : aUserIds) {
                topics.add(topic(aAppId, id));
            }

            return publisher.unsubscribe(topics, aSubscriberId);
        });
    }

    private TopicName topic(final ApplicationId aAppId, final UserId aUserId) {
        return new TopicName().append(NAMESPACE).append(aAppId.value()).append(hashedId(aUserId.value()));
    }

    private String hashedId(String aId) {
        return Utils.hashMD5(aId);
    }
}
