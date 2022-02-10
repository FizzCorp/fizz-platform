package io.fizz.chat.application.channel;

import com.google.gson.Gson;
import com.newrelic.api.agent.NewRelic;
import io.fizz.chat.application.UserNotificationApplicationService;
import io.fizz.chat.application.impl.ApplicationService;
import io.fizz.chat.application.services.AbstractPushNotificationService;
import io.fizz.chat.application.services.TranslationService;
import io.fizz.chat.domain.channel.*;
import io.fizz.chat.domain.subscriber.AbstractSubscriptionService;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chat.moderation.application.service.ContentModerationService;
import io.fizz.chat.presence.AbstractUserPresenceService;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.topic.TopicMessage;
import io.fizz.chat.pubsub.domain.topic.TopicName;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.AbstractEventListener;
import io.fizz.chatcommon.domain.events.DomainEventBus;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.common.LoggingService;
import io.fizz.common.NewRelicErrorPriority;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

class ChannelEventsListener implements AbstractEventListener {
    private static class MessageData {
        public String nick;
        public String to;
        public String body;
        public String data;
        public Map<String,String> translations;
        public long updated;

        MessageData(String aNick,
                    String aTo,
                    String aBody,
                    String aData,
                    Map<LanguageCode,String> aTranslations,
                    long aUpdated) {
            nick = aNick;
            to = aTo;
            body = aBody;
            data = aData;
            updated = aUpdated;

            if (Objects.isNull(aTranslations)) {
                translations = null;
            }
            else {
                translations = new HashMap<>();
                for (Map.Entry<LanguageCode,String> entry: aTranslations.entrySet()) {
                    translations.put(entry.getKey().value(), entry.getValue());
                }
            }
        }
    }

    private static final LoggingService.Log logger = new LoggingService.Log(ChannelEventsListener.class);

    private static final DomainEventType[] events = new DomainEventType[] {
        ChannelMessageReceivedForPublish.TYPE,
        ChannelMessageReceivedForUpdate.TYPE,
        ChannelMessageReceivedForDeletion.TYPE
    };
    private static final int MaxRetryCount = 3;

    private final DomainEventBus eventBus;
    private final AbstractTopicMessagePublisher messagePublisher;
    private final AbstractChannelMessageRepository messageRepo;
    private final ApplicationService appService;
    private final TranslationService translationService;
    private final ContentModerationService contentModerationService;
    private final AbstractSubscriptionService subscriptionService;
    private final AbstractUserPresenceService presenceService;
    private final UserNotificationApplicationService userNotificationService;
    private final AbstractPushNotificationService pushNotificationService;

    ChannelEventsListener(final DomainEventBus aEventBus,
                          final AbstractChannelMessageRepository aMessageRepo,
                          final AbstractTopicMessagePublisher aMessagePublisher,
                          final ApplicationService aAppService,
                          final TranslationService aTranslationService,
                          final ContentModerationService aContentModerationService,
                          final AbstractSubscriptionService aSubscriptionService,
                          final AbstractUserPresenceService aPresenceService,
                          final UserNotificationApplicationService aUserNotificationService,
                          final AbstractPushNotificationService aPushNotificationService) {
        Utils.assertRequiredArgument(aEventBus, "invalid domain event bus");
        Utils.assertRequiredArgument(aMessageRepo, "invalid message repository");
        Utils.assertRequiredArgument(aMessagePublisher, "invalid message publisher");
        Utils.assertRequiredArgument(aAppService, "invalid application service");
        Utils.assertRequiredArgument(aTranslationService, "invalid translation service");
        Utils.assertRequiredArgument(aContentModerationService, "invalid content moderation service");
        Utils.assertRequiredArgument(aSubscriptionService, "invalid subscription service");
        Utils.assertRequiredArgument(aUserNotificationService, "invalid user notification service");

        eventBus = aEventBus;
        messageRepo = aMessageRepo;
        messagePublisher = aMessagePublisher;
        appService = aAppService;
        translationService = aTranslationService;
        contentModerationService = aContentModerationService;
        subscriptionService = aSubscriptionService;
        presenceService = aPresenceService;
        userNotificationService = aUserNotificationService;
        pushNotificationService = aPushNotificationService;
    }

    @Override
    public DomainEventType[] listensTo() {
        return events;
    }

    @Override
    public CompletableFuture<Void> handleEvent(AbstractDomainEvent aEvent) {
        CompletableFuture<Void> future;
        if (aEvent.type().equals(ChannelMessageReceivedForPublish.TYPE)) {
            future = on((ChannelMessageReceivedForPublish)aEvent);
        }
        else
        if (aEvent.type().equals(ChannelMessageReceivedForUpdate.TYPE)) {
            future = on((ChannelMessageReceivedForUpdate)aEvent);
        }
        else
        if (aEvent.type().equals(ChannelMessageReceivedForDeletion.TYPE)) {
            future = on((ChannelMessageReceivedForDeletion) aEvent);
        } else {
            future = CompletableFuture.completedFuture(null);
        }

        return future.handle((result, error) -> {
            if (Objects.nonNull(error)) {
                logger.error(error);
                NewRelic.noticeError(error,
                        Utils.buildNewRelicErrorMap(NewRelicErrorPriority.HIGH,
                                "ChannelEventListener::handleEvent"));
            }
            return null;
        });
    }

    private CompletableFuture<Void> on(final ChannelMessageReceivedForPublish aEvent) {
        return filter(aEvent, aEvent.message().body())
                .thenCompose(aFilteredMessage -> translate(aEvent, aFilteredMessage.body()))
                .thenCompose(aTranslatedMessage -> publishMessage(aEvent, aTranslatedMessage));
    }

    private CompletableFuture<Void> on(final ChannelMessageReceivedForUpdate aEvent) {
        return filter(aEvent, aEvent.message().body())
                .thenCompose(aFilteredMessage -> translate(aEvent, aFilteredMessage.body()))
                .thenCompose(aTranslatedMessage -> updateMessage(aEvent, aTranslatedMessage));
    }

    private CompletableFuture<Void> on(final ChannelMessageReceivedForDeletion aEvent) {
        if (!aEvent.message().deleted()) {
            logger.warn("Received message that was not marked as deleted");
        }

        final ApplicationId appId = aEvent.appId();
        final TopicId topicId = aEvent.topicId();
        final ChannelMessage message = aEvent.message();

        return messageRepo.remove(appId, topicId, message.id())
                .thenCompose(v -> publishEvent(appId, topicId, message, ChannelEventType.MessageDeleted))
                .thenCompose(v -> notifyUserEvent(appId, aEvent.notifyList(), message, ChannelEventType.MessageDeleted));
    }

    private CompletableFuture<Void> publishEvent(final ApplicationId aAppId,
                                                 final TopicId aTopicId,
                                                 final ChannelMessage aMessage,
                                                 ChannelEventType aEventType) {
        try {
            final TopicMessage topicMessage = adaptTo(aMessage, aEventType);
            final TopicName topic = new TopicName().append(aAppId.value()).append(aTopicId.value());
            CompletableFuture<Void> future = messagePublisher.publish(Collections.singleton(topic), topicMessage);
            for (int i = 1; i < MaxRetryCount; i ++) {
                future = future.thenApply(CompletableFuture::completedFuture)
                            .exceptionally(t -> messagePublisher.publish(Collections.singleton(topic), topicMessage))
                            .thenCompose(Function.identity());
            }

            return future;
        }
        catch (IllegalArgumentException ex) {
            return Utils.failedFuture(ex);
        }
    }

    private CompletableFuture<Void> publishMessage(final ChannelMessageReceivedForPublish aEvent,
                                                  final ChannelMessage aMessage) {
        final ApplicationId appId = aEvent.appId();
        final TopicId topicId = aEvent.topicId();

        return persistMessage(aEvent, aMessage)
                .thenCompose(v -> publishEvent(appId, topicId, aMessage, ChannelEventType.MessagePublished))
                .thenCompose(v -> notifyUserEvent(appId, aEvent.notifyList(), aMessage, ChannelEventType.MessagePublished))
                .thenCompose(v -> sendPushNotification(appId, aEvent.notifyList(), aMessage));
    }

    private CompletableFuture<Boolean> persistMessage(final ChannelMessageReceivedForPublish aEvent,
                                                      final ChannelMessage aMessage) {
        final ApplicationId appId = aEvent.appId();
        final TopicId topicId = aEvent.topicId();
        if (!aEvent.persist()) {
            return CompletableFuture.completedFuture(true);
        }
        return messageRepo.save(appId, topicId, aMessage);
    }

    private CompletableFuture<Void> updateMessage(final ChannelMessageReceivedForUpdate aEvent,
                                                  final ChannelMessage aMessage) {
        final ApplicationId appId = aEvent.appId();
        final TopicId topicId = aEvent.topicId();

        return messageRepo
        .update(appId, topicId, aMessage)
        .thenCompose(status -> {
            if (!status) {
                logger.warn("Unable to update message: " + aEvent.toString());
                return CompletableFuture.completedFuture(null);
            } else {
                return publishEvent(appId, topicId, aMessage, ChannelEventType.MessageUpdated)
                        .thenCompose(v -> notifyUserEvent(appId, aEvent.notifyList(), aMessage, ChannelEventType.MessageUpdated));
            }
        });
    }

    private CompletableFuture<Void> notifyUserEvent(final ApplicationId aAppId,
                                                    final Set<UserId> aNotifyList,
                                                    final ChannelMessage aMessage,
                                                    final ChannelEventType aEventType) {
        try {
            if (Objects.isNull(aNotifyList) || aNotifyList.size() == 0) {
                return CompletableFuture.completedFuture(null);
            }
            final TopicMessage topicMessage = adaptTo(aMessage, aEventType);
            return userNotificationService.publish(aAppId, aNotifyList, topicMessage);
        }
        catch (IllegalArgumentException ex) {
            return Utils.failedFuture(ex);
        }
    }

    private CompletableFuture<Void> sendPushNotification(final ApplicationId aAppId,
                                                         final Set<UserId> aNotifyList,
                                                         final ChannelMessage aMessage) {
        if (Objects.isNull(aNotifyList) || aNotifyList.size() == 0) {
            return CompletableFuture.completedFuture(null);
        }
        return presenceService.getOfflineUsers(aAppId, aNotifyList).thenCompose(offlineUsers -> {
            if (offlineUsers.size() == 0) {
                return CompletableFuture.completedFuture(null);
            }
            return pushNotificationService.send(aAppId, aMessage, offlineUsers);
        });
    }

    private CompletableFuture<ChannelMessage> translate(final AbstractChannelMessageEvent aEvent,
                                                        final String aText) {
        final ChannelMessage message = duplicateMessage(aEvent.message(), aText);
        if (!aEvent.translate() || aText.length() == 0) {
            return CompletableFuture.completedFuture(message);
        }

        return subscriptionService
            .fetchLocales(aEvent.message().to())
            .thenCompose(aLocales -> translationService.translate(aText, message.locale(), aLocales.toArray(new LanguageCode[0])))
            .thenCompose(aTranslations -> {
                for (Map.Entry<LanguageCode,String> entry: aTranslations.entrySet()) {
                    message.setTranslation(entry.getKey(), entry.getValue());
                }
                return CompletableFuture.completedFuture(message);
            })
            .thenCompose(aMessages -> publishTranslationEvent(aEvent)
            .thenApply(v -> message))
            .handle((aResult, aError) -> {
                if (Objects.nonNull(aError)) {
                    logger.warn(aError);
                }
                return Objects.isNull(aError) ? aResult : message;
            });
    }

    private CompletableFuture<ChannelMessage> filter(final AbstractChannelMessageEvent aEvent, final String aText) {
        final ChannelMessage message = duplicateMessage(aEvent.message(), aText);

        if (aText.length() == 0) {
            return CompletableFuture.completedFuture(message);
        }

        final ApplicationId appId = aEvent.appId();
        return appService.getPreferences(appId)
                .thenCompose(aPrefs -> {
                    boolean internal = false;
                    if (aEvent.type().equals(ChannelMessageReceivedForPublish.TYPE)) {
                        final ChannelMessageReceivedForPublish pubEvent = (ChannelMessageReceivedForPublish)aEvent;
                        internal = pubEvent.isInternal();
                    }

                    if (internal || (!aPrefs.isForceContentModeration() && !aEvent.filter())) {
                        return CompletableFuture.completedFuture(message);
                    }

                    return contentModerationService.filter(aEvent.appId(), aText)
                            .handle((aFilteredText, aError) -> {
                                if (Objects.nonNull(aError)) {
                                    logger.warn(aError);
                                }
                                final String filteredText = Objects.isNull(aError) ? aFilteredText : message.body();
                                final ChannelMessage filteredMessage = new ChannelMessage(
                                        message.id(),
                                        message.from(),
                                        message.nick(),
                                        message.to(),
                                        message.topic(),
                                        filteredText,
                                        message.data(),
                                        message.locale(),
                                        new Date(message.created()),
                                        message.translations());

                                return filteredMessage;
                            });
                });
    }

    private TopicMessage adaptTo(final ChannelMessage aMessage,
                                 final ChannelEventType aType) throws IllegalArgumentException {
        final MessageData data = new MessageData(
            aMessage.nick(),
            aMessage.to().value(),
            aMessage.body(),
            aMessage.data(),
            aMessage.translations(),
            aMessage.updated()
        );

        return new TopicMessage(
            aMessage.id(),
            aType.value(),
            aMessage.from().value(),
            new Gson().toJson(data),
            new Date()
        );
    }

    private CompletableFuture<Void> publishTranslationEvent(final AbstractChannelMessageEvent aEvent) {
        if (Objects.isNull(aEvent.message().translations())) {
            return CompletableFuture.completedFuture(null);
        }
        ChannelMessageTranslated translationEvent = buildMessageTranslatedEvent(aEvent);
        return eventBus.publish(translationEvent);
    }

    private ChannelMessageTranslated buildMessageTranslatedEvent(final AbstractChannelMessageEvent aEvent) {
        ChannelMessage message = aEvent.message();
        return new ChannelMessageTranslated.Builder()
                .setId(UUID.randomUUID().toString())
                .setAppId(aEvent.appId())
                .setLength(message.body().length())
                .setOccurredOn(new Date().getTime())
                .setChannelId(message.topic().value())
                .setMessageId(aEvent.appId().value() + "_" + message.topic().value() + "_" + message.id())
                .setUserId(message.from())
                .setTo(message.translations().keySet().toArray(new LanguageCode[0]))
                .get();
    }

    private ChannelMessage duplicateMessage(final ChannelMessage aMessage, final String aBody) {
        return new ChannelMessage(
                aMessage.id(),
                aMessage.from(),
                aMessage.nick(),
                aMessage.to(),
                aMessage.topic(),
                aBody,
                aMessage.data(),
                aMessage.locale(),
                new Date(aMessage.created()),
                aMessage.translations());
    }
}
