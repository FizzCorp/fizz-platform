package io.fizz.chat.application.channel;

import io.fizz.chat.application.ChannelAuthorizationService;
import io.fizz.chat.application.UserNotificationApplicationService;
import io.fizz.chat.application.impl.ApplicationService;
import io.fizz.chat.application.services.AbstractPushNotificationService;
import io.fizz.chat.application.services.TranslationService;
import io.fizz.chat.domain.Permissions;
import io.fizz.chat.domain.subscriber.AbstractSubscriptionService;
import io.fizz.chat.presence.AbstractUserPresenceService;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.moderation.application.service.ContentModerationService;
import io.fizz.chat.pubsub.domain.topic.TopicName;
import io.fizz.chataccess.domain.AbstractAuthorizationService;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.chatcommon.domain.events.DomainEventBus;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.chat.domain.channel.*;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ChannelApplicationService {
    private final DomainEventBus eventBus;
    private final AbstractTopicMessagePublisher messagePublisher;
    private final AbstractChannelMessageRepository messageRepo;
    private final ChannelEventsListener messageReceivedListener;
    private final AbstractChannelAuthorizationService authService;
    private final AbstractSubscriptionService subscriptionService;

    public ChannelApplicationService(final DomainEventBus aEventBus,
                                     final AbstractTopicMessagePublisher aMessagePublisher,
                                     final AbstractChannelMessageRepository aMessageRepo,
                                     final ApplicationService aAppService,
                                     final TranslationService aTranslationService,
                                     final ContentModerationService aContentModerationService,
                                     final AbstractAuthorizationService aAuthService,
                                     final AbstractSubscriptionService aSubscriptionService,
                                     final AbstractUserPresenceService aPresenceService,
                                     final UserNotificationApplicationService aUserNotificationService,
                                     final AbstractPushNotificationService aPushNotificationService,
                                     final RoleName aMutesRole,
                                     final RoleName aBanRole) {
        Utils.assertRequiredArgument(aEventBus, "invalid domain event bus");
        Utils.assertRequiredArgument(aMessagePublisher, "invalid message publisher");
        Utils.assertRequiredArgument(aMessageRepo, "invalid message repo");
        Utils.assertRequiredArgument(aAppService, "invalid application service");
        Utils.assertRequiredArgument(aTranslationService, "invalid translation service");
        Utils.assertRequiredArgument(aContentModerationService, "invalid content moderation service");
        Utils.assertRequiredArgument(aAuthService, "invalid auth service");
        Utils.assertRequiredArgument(aSubscriptionService, "invalid subscription service");
        Utils.assertRequiredArgument(aPresenceService, "invalid presence service");
        Utils.assertRequiredArgument(aUserNotificationService, "invalid user notification service");
        Utils.assertRequiredArgument(aPushNotificationService, "invalid push notification service");

        eventBus = aEventBus;
        messageRepo = aMessageRepo;
        messagePublisher = aMessagePublisher;
        authService = new ChannelAuthorizationService(aAuthService, aMutesRole, aBanRole);
        subscriptionService = aSubscriptionService;
        messageReceivedListener = new ChannelEventsListener(
                eventBus,
                messageRepo,
                messagePublisher,
                aAppService,
                aTranslationService,
                aContentModerationService,
                subscriptionService,
                aPresenceService,
                aUserNotificationService,
                aPushNotificationService
        );
    }

    public CompletableFuture<Void> open() {
        return eventBus.addListener(messageReceivedListener);
    }

    public CompletableFuture<Void> close() {
        return eventBus.removeListener(messageReceivedListener);
    }

    public CompletableFuture<Void> publish(final ChannelMessagePublishCommand aCmd,
                                           final String aSubscriberId,
                                           final String aLocale) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aCmd, "invalid_command");

            return map(aCmd.channelId(), aSubscriberId, aLocale).thenCompose(aTopicId -> publish(aCmd, aTopicId));
        });
    }

    public CompletableFuture<Void> publish(final ChannelMessagePublishCommand aCmd) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aCmd, "invalid_command");

            return publish(aCmd, subscriptionService.defaultTopic(aCmd.channelId()));
        });
    }

    public CompletableFuture<Void> publish(final ChannelMessagePublishCommand aCmd, final String aTopicId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aCmd, "invalid_command");

            return subscriptionService.fetchTopic(aCmd.channelId(), aTopicId)
                                    .thenCompose(aChannelTopicId -> publish(aCmd, aChannelTopicId));
        });
    }

    public CompletableFuture<Void> update(final ChannelMessageUpdateCommand aCmd) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aCmd, "invalid_command");

            return update(aCmd, subscriptionService.defaultTopic(aCmd.channelId()));
        });
    }

    private CompletableFuture<Void> update(final ChannelMessageUpdateCommand aCmd, final TopicId aTopicId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aCmd, "invalid_command");

            final Channel channel = new Channel(aCmd.channelId());

            return messageRepo
                    .messageOfId(aCmd.appId(), aTopicId, aCmd.messageId())
                    .thenCompose(
                        aMessage -> channel.editMessage(
                            authService, aMessage, aCmd.authorId(), aCmd.nick(), aCmd.body(), aCmd.data(), aCmd.locale()
                        )
                    )
                    .thenCompose(
                        aMessage -> eventBus.publish(
                            new ChannelMessageReceivedForUpdate(
                                aCmd.appId(),
                                aTopicId,
                                aMessage,
                                !Objects.isNull(aCmd.body()) && aCmd.translate(),
                                !Objects.isNull(aCmd.body()) && aCmd.filter(),
                                new Date(),
                                aCmd.notifyList()
                            )
                        )
                    );
        });
    }

    public CompletableFuture<Void> delete(final ChannelMessageDeleteCommand aCmd) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aCmd, "invalid_command");

            return delete(aCmd, subscriptionService.defaultTopic(aCmd.channelId()));
        });
    }

    private CompletableFuture<Void> delete(final ChannelMessageDeleteCommand aCmd, final TopicId aTopicId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aCmd, "invalid_command");

            final Channel channel = new Channel(aCmd.channelId());

            return messageRepo
                    .messageOfId(aCmd.appId(), aTopicId, aCmd.messageId())
                    .thenCompose(aMessage -> channel.deleteMessage(authService, aMessage, aCmd.authorId()))
                    .thenCompose(aMessage -> eventBus.publish(
                        new ChannelMessageReceivedForDeletion(aCmd.appId(), aTopicId, aMessage, new Date(), aCmd.notifyList())
                    ));
        });
    }

    public CompletableFuture<List<ChannelMessage>> queryMessages(final ChannelHistoryQuery aQuery,
                                                                 final String aSubscriberId,
                                                                 final String aLocale) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aQuery, "invalid_query");


            return map(aQuery.channelId(), aSubscriberId, aLocale)
                    .thenCompose(aTopicId -> queryMessages(aQuery, aTopicId));
        });
    }

    public CompletableFuture<List<ChannelMessage>> queryMessages(final ChannelHistoryQuery aQuery) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aQuery, "invalid_query");

            return queryMessages(aQuery, subscriptionService.defaultTopic(aQuery.channelId()));
        });
    }

    public CompletableFuture<List<ChannelMessage>> queryMessages(final ChannelHistoryQuery aQuery,
                                                                 final String aTopicId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aQuery, "invalid_query");
            Utils.assertRequiredArgument(aTopicId, "invalid_topic_id");

            return subscriptionService.fetchTopic(aQuery.channelId(), aTopicId)
                    .thenCompose(topicId -> queryMessages(aQuery, topicId));
        });
    }

    public CompletableFuture<Void> subscribe(final ChannelSubscribeCommand aCmd) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aCmd, "invalid_command");

            return authService
                .assertOperateOwned(aCmd.channelId(), aCmd.authorId(), aCmd.authorId(), Permissions.READ_MESSAGES)
                .thenCompose(
                    v -> subscriptionService.assignOrFetchTopic(aCmd.channelId(), aCmd.subscriberId(), aCmd.locale())
                )
                .thenCompose(aTopicId -> {
                    final TopicName topic = new TopicName().append(aCmd.appId().value()).append(aTopicId.value());

                    return messagePublisher.subscribe(Collections.singleton(topic), aCmd.subscriberId());
                });
        });
    }

    public CompletableFuture<Void> unsubscribe(final ChannelUnsubscribeCommand aCmd) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aCmd, "invalid_command");

            return authService
                .assertOperateOwned(aCmd.channelId(), aCmd.authorId(), aCmd.authorId(), Permissions.READ_MESSAGES)
                .thenCompose(
                    v -> subscriptionService.assignOrFetchTopic(aCmd.channelId(), aCmd.subscriberId(), aCmd.locale())
                )
                .thenCompose(aTopicId -> {
                    final TopicName topic = new TopicName().append(aCmd.appId().value()).append(aTopicId.value());

                    return messagePublisher.unsubscribe(Collections.singleton(topic), aCmd.subscriberId());
                });
        });
    }

    public CompletableFuture<Void> addUserBan(final String aAppId,
                                              final String aChannelId,
                                              final String aModerator,
                                              final String aMutedUser,
                                              final Date aEnds) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            final ChannelId channelId = new ChannelId(appId, aChannelId);
            final Channel channel = new Channel(channelId);
            final UserId moderator = new UserId(aModerator);
            final UserId mutedUser = new UserId(aMutedUser);

            return channel.addUserBan(authService, moderator, mutedUser, aEnds);
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<Void> removeUserBan(final String aAppId,
                                                 final String aChannelId,
                                                 final String aModerator,
                                                 final String aMutedUser) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            final ChannelId channelId = new ChannelId(appId, aChannelId);
            final Channel channel = new Channel(channelId);
            final UserId moderator = new UserId(aModerator);
            final UserId bannedUser = new UserId(aMutedUser);

            return channel.removeUserBan(authService, moderator, bannedUser);
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<Void> muteUser(final String aAppId,
                                            final String aChannelId,
                                            final String aModerator,
                                            final String aMutedUser,
                                            final Date aEnds) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            final ChannelId channelId = new ChannelId(appId, aChannelId);
            final Channel channel = new Channel(channelId);
            final UserId moderator = new UserId(aModerator);
            final UserId mutedUser = new UserId(aMutedUser);

            return channel.muteUser(authService, moderator, mutedUser, aEnds);
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<Void> unmuteUser(final String aAppId,
                                              final String aChannelId,
                                              final String aModerator,
                                              final String aMutedUser) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            final ChannelId channelId = new ChannelId(appId, aChannelId);
            final Channel channel = new Channel(channelId);
            final UserId moderator = new UserId(aModerator);
            final UserId bannedUser = new UserId(aMutedUser);

            return channel.unmuteUser(authService, moderator, bannedUser);
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    public CompletableFuture<Set<TopicId>> fetchTopics(final String aAppId,
                                                        final String aUserId,
                                                        final String aChannelId) {
        try {
            final ApplicationId appId = new ApplicationId(aAppId);
            final UserId userId = new UserId(aUserId);
            final ChannelId channelId = new ChannelId(appId, aChannelId);
            return authService
                    .assertOperateOwned(channelId, userId, userId, Permissions.READ_CHANNEL_TOPICS)
                    .thenCompose(v -> subscriptionService.fetchTopics(channelId));
        }
        catch (Exception ex) {
            return Utils.failedFuture(ex);
        }
    }

    private CompletableFuture<Void> publish(final ChannelMessagePublishCommand aCmd, final TopicId aTopicId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aCmd, "invalid_command");

            final Channel channel = new Channel(aCmd.channelId());

            return channel
                    .publishMessage(
                            authService,
                            messageRepo,
                            aTopicId,
                            aCmd.authorId(),
                            aCmd.nick(),
                            aCmd.body(),
                            aCmd.data(),
                            aCmd.locale()
                    )
                    .thenCompose(aMessage -> eventBus.publish(
                        new ChannelMessageReceivedForPublish(
                            aCmd.appId(),
                            aTopicId,
                            aMessage,
                            !Objects.isNull(aCmd.body()) && aCmd.isTranslate(),
                            !Objects.isNull(aCmd.body()) && aCmd.isFilter(),
                            aCmd.isPersist(),
                            new Date(),
                            aCmd.notifyList(),
                            aCmd.isInternal()
                        )
                    ));
        });
    }

    private CompletableFuture<List<ChannelMessage>> queryMessages(final ChannelHistoryQuery aQuery,
                                                                  final TopicId aTopicId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aQuery, "invalid_query");
            Utils.assertRequiredArgument(aTopicId, "invalid_topic_id");

            final Channel channel = new Channel(aQuery.channelId());

            return channel.listMessages(
                    authService, messageRepo, aQuery.requesterId(), aTopicId, aQuery.count(), aQuery.before(), aQuery.after()
            );
        });
    }

    private CompletableFuture<TopicId> map(final ChannelId aChannelId, final String aSubId, final String aLocale) {
        try {
            final SubscriberId subId = new SubscriberId(aSubId);
            final LanguageCode locale = LanguageCode.fromValue(aLocale);

            return subscriptionService.assignOrFetchTopic(aChannelId, subId, locale);
        }
        catch (DomainErrorException ex) {
            return Utils.failedFuture(ex);
        }
    }
}
