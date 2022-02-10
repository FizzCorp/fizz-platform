package io.fizz.chat.application.channel;

import io.fizz.chat.application.AbstractApplicationRepository;
import io.fizz.chat.application.Preferences;
import io.fizz.chat.application.UserNotificationApplicationService;
import io.fizz.chat.application.impl.ApplicationService;
import io.fizz.chat.application.impl.HBaseApplicationRepository;
import io.fizz.chat.application.impl.MockApplicationService;
import io.fizz.chat.application.services.AbstractPushNotificationService;
import io.fizz.chat.application.services.TranslationService;
import io.fizz.chat.domain.channel.*;
import io.fizz.chat.domain.subscriber.AbstractSubscriptionService;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chat.infrastructure.persistence.HBaseChannelMessageRepository;
import io.fizz.chat.infrastructure.services.HBaseUIDService;
import io.fizz.chat.infrastructure.services.MockPushNotificationService;
import io.fizz.chat.infrastructure.services.MockSubscriptionService;
import io.fizz.chat.infrastructure.services.MockTranslationClient;
import io.fizz.chat.moderation.application.service.ContentModerationService;
import io.fizz.chat.moderation.application.service.MockContentModerationService;
import io.fizz.chat.presence.AbstractUserPresenceService;
import io.fizz.chat.presence.infrastructure.presence.MockUserPresenceService;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.subscriber.AbstractSubscriberRepository;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.pubsub.domain.topic.TopicMessage;
import io.fizz.chat.pubsub.domain.topic.TopicName;
import io.fizz.chat.pubsub.infrastructure.messaging.MockTopicMessagePublisher;
import io.fizz.chataccess.domain.MockAuthorizationService;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.AbstractEventListener;
import io.fizz.chatcommon.domain.events.DomainEventBus;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.chatcommon.infrastructure.messaging.MockEventPublisher;
import io.fizz.chatcommon.infrastructure.serde.JsonEventSerde;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.client.MockHBaseClient;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
class ChannelApplicationServiceTest {
    private static class Subscriber {
        private static final IllegalArgumentException ERROR_INVALID_USER_ID = new IllegalArgumentException("invalid_user_id");
        private static final IllegalArgumentException ERROR_INVALID_LOCALE = new IllegalArgumentException("invalid_subscriber_locale");

        private final SubscriberId id;
        private final UserId userId;
        private final LanguageCode locale;

        public Subscriber(final SubscriberId aId,
                          final UserId aUserId,
                          final LanguageCode aLocale) {
            Utils.assertRequiredArgument(aId, SubscriberId.ERROR_INVALID_VALUE);
            Utils.assertRequiredArgument(aUserId, ERROR_INVALID_USER_ID);
            Utils.assertRequiredArgument(aLocale, ERROR_INVALID_LOCALE);

            id = aId;
            userId = aUserId;
            locale = aLocale;
        }

        public SubscriberId id() {
            return id;
        }

        public UserId userId() {
            return userId;
        }

        public LanguageCode locale() {
            return locale;
        }
    }


    private static final int TEST_TIMEOUT = 5;
    private ApplicationId appId;
    private ApplicationService appService;
    private DomainEventBus defaultEventBus;
    private HBaseChannelMessageRepository defaultMessageRepo;
    private AbstractSubscriptionService subscriptionService;
    private AbstractSubscriberRepository subscriberRepo;
    private AbstractUserPresenceService presenceService;
    private ContentModerationService moderationService;
    private UserNotificationApplicationService userNotificationService;
    private AbstractPushNotificationService pushNotificationService;

    private static Vertx vertx;

    @BeforeEach
    void setUp(VertxTestContext aContext) {
        vertx = Vertx.vertx();

        try {
            appId = new ApplicationId("testApp");

            AbstractHBaseClient defaultClient = new MockHBaseClient();
            HBaseUIDService defaultIdService = new HBaseUIDService(defaultClient);
            final AbstractApplicationRepository appRepo = new HBaseApplicationRepository(defaultClient);

            appService = new MockApplicationService(appRepo);
            defaultEventBus = new DomainEventBus();
            subscriptionService = new MockSubscriptionService();
            subscriberRepo = new MockSubscriptionService();
            presenceService = new MockUserPresenceService();
            pushNotificationService = new MockPushNotificationService();
            MockEventPublisher defaultEventPublisher = new MockEventPublisher(new JsonEventSerde(),
                    new DomainEventType[]{
                            ChannelMessageReceivedForPublish.TYPE,
                            ChannelMessageReceivedForUpdate.TYPE,
                            ChannelMessageReceivedForDeletion.TYPE,
                            ChannelMessageTranslated.TYPE
                    });
            defaultEventBus.register(defaultEventPublisher);
            defaultMessageRepo = new HBaseChannelMessageRepository(
                    defaultClient, defaultIdService, new MockSubscriptionService()
            );
            moderationService = new MockContentModerationService(
                    appService, vertx);
            userNotificationService = new UserNotificationApplicationService(new MockTopicMessagePublisher());
        }
        catch (DomainErrorException err) {
            System.out.println(err.getMessage());
        }

        aContext.completeNow();
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        vertx.close(res -> aContext.completeNow());
    }

    @Test
    @DisplayName("it should return message in the same order they were published")
    void messageHistoryValidationTest(final VertxTestContext aContext) throws InterruptedException {
        TopicId topicId = new TopicId(UUID.randomUUID().toString());
        ChannelMessage message1 = buildMessage(topicId, 1, "userA", "message 1", "data 1");
        ChannelMessage message2 = buildMessage(topicId, 2, "userB", "message 2", "data 2");
        ChannelMessage message3 = buildMessage(topicId, 3, "userA", "message 3", "data 3");

        final AbstractTopicMessagePublisher messagePublisher = buildMessagePublisher();
        final ContentModerationService contentModService = contentModerationService();
        final ChannelApplicationService channelService = buildChannelAppService(messagePublisher, contentModService);

        channelService.open()
        .thenCompose(v -> publish(channelService, appId, topicId, message1, false, false, new UserId("userA")))
        .thenCompose(v -> publish(channelService, appId, topicId, message2, false, false, new UserId("userB")))
        .thenCompose(v -> publish(channelService, appId, topicId, message3, false, false, new UserId("userC")))
        .thenCompose(v -> channelService.queryMessages(
                new ChannelHistoryQuery(
                        new ChannelId(appId,  topicId.value()), "userC",3, null, null
                )
            )
        )
        .handle((aMessages, aError) -> {
            Assertions.assertTrue(Objects.isNull(aError));
            Assertions.assertEquals(aMessages.size(), 3);
            Assertions.assertTrue(messageEquals(message1, aMessages.get(0)));
            Assertions.assertTrue(messageEquals(message2, aMessages.get(1)));
            Assertions.assertTrue(messageEquals(message3, aMessages.get(2)));
            aContext.completeNow();
            return CompletableFuture.completedFuture(null);
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should publish event for text message translated")
    void messageTranslationEventValidationTest(final VertxTestContext aContext) throws InterruptedException {
        TopicId topicId = new TopicId(UUID.randomUUID().toString());
        ChannelMessage message1 = buildMessage(topicId, 1, "userA", "message 1", "data 1");

        final AbstractTopicMessagePublisher messagePublisher = buildMessagePublisher();
        final ContentModerationService contentModService = contentModerationService();
        final ChannelApplicationService channelService = buildChannelAppService(messagePublisher, contentModService);

        defaultEventBus.addListener(new AbstractEventListener() {
            @Override
            public CompletableFuture<Void> handleEvent(AbstractDomainEvent aEvent) {
                if (aEvent instanceof ChannelMessageTranslated) {
                    ChannelMessageTranslated messageTranslated = (ChannelMessageTranslated) aEvent;
                    Assertions.assertEquals(1, messageTranslated.messageId());
                    Assertions.assertEquals(9, messageTranslated.length());
                }

                return CompletableFuture.completedFuture(null);
            }

            @Override
            public DomainEventType[] listensTo() {
                return new DomainEventType[]{ChannelMessageTranslated.TYPE};
            }
        });

        channelService.open()
        .thenCompose(v -> publish(channelService, appId, topicId, message1, true, false, new UserId("userA")))
        .handle((aMessages, aError) -> {
            Assertions.assertTrue(Objects.isNull(aError));
            aContext.completeNow();
            return CompletableFuture.completedFuture(null);
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should return message in the same order they were published")
    void messagePublishValidationTest(final VertxTestContext aContext) throws InterruptedException {
        TopicId topicId = new TopicId(UUID.randomUUID().toString());
        ChannelMessage message1 = buildMessage(topicId, 1, "userA", "message 1", "data 1");
        ChannelMessage message2 = buildMessage(topicId, 2, "userB", "message 2", "data 2");
        ChannelMessage message3 = buildMessage(topicId, 3, "userA", "Filter 3", "data 3");
        ChannelMessage message3_filter = buildMessage(topicId, 3, "userA", "***", "data 3");

        final int[] msgsCheck = new int[3];
        final AbstractTopicMessagePublisher messagePublisher =
            new AbstractTopicMessagePublisher() {
                @Override
                public CompletableFuture<Void> publish(Set<TopicName> aTopics, TopicMessage aMessage) {
                    try {
                        if (messageEquals(message1, aMessage)) {
                            msgsCheck[0]++;
                        } else if (messageEquals(message2, aMessage)) {
                            msgsCheck[1]++;
                        } else if (messageEquals(message3_filter, aMessage)) {
                            msgsCheck[2]++;
                        }
                    } catch (Exception ex) {
                        aContext.failNow(ex);
                    }

                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public CompletableFuture<Void> subscribe(Set<TopicName> aTopics, SubscriberId aSubscriber) {
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public CompletableFuture<Void> unsubscribe(Set<TopicName> aTopics, SubscriberId aId) {
                    return CompletableFuture.completedFuture(null);
                }
            };
        final ContentModerationService contentModService = contentModerationService();
        final ChannelApplicationService channelService = buildChannelAppService(messagePublisher, contentModService);

        channelService.open()
        .thenCompose(v -> publish(channelService, appId, topicId, message1, false, false, new UserId("userA")))
        .thenCompose(v -> publish(channelService, appId, topicId, message2, false, true, new UserId("userB")))
        .thenCompose(v -> publish(channelService, appId, topicId, message3, false, true, new UserId("userC")))
        .handle((v, aError) -> {
            if (!Objects.isNull(aError)) {
                aContext.failNow(aError);
            }
            for (int msgCheck: msgsCheck) {
                Assertions.assertEquals(1, msgCheck);
            }
            aContext.completeNow();
            return CompletableFuture.completedFuture(null);
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should filter message if force content moderation is set")
    void forceContentModerationValidationTest(final VertxTestContext aContext) throws InterruptedException, ExecutionException {
        TopicId topicId = new TopicId(UUID.randomUUID().toString());
        ChannelMessage message1 = buildMessage(topicId, 1, "userA", "message 1", "data 1");
        ChannelMessage message2 = buildMessage(topicId, 2, "userB", "message 2", "data 2");
        ChannelMessage message3 = buildMessage(topicId, 3, "userA", "Filter 3", "data 3");
        ChannelMessage message3_filter = buildMessage(topicId, 3, "userA", "***", "data 3");

        Preferences prefs = new Preferences();
        prefs.setForceContentModeration(true);
        appService.updatePreferences(appId, prefs).get();

        final int[] msgsCheck = new int[3];
        final AbstractTopicMessagePublisher messagePublisher =
                new AbstractTopicMessagePublisher() {
                    @Override
                    public CompletableFuture<Void> publish(Set<TopicName> aTopics, TopicMessage aMessage) {
                        try {
                            if (messageEquals(message1, aMessage)) {
                                msgsCheck[0]++;
                            } else if (messageEquals(message2, aMessage)) {
                                msgsCheck[1]++;
                            } else if (messageEquals(message3_filter, aMessage)) {
                                msgsCheck[2]++;
                            }
                        } catch (Exception ex) {
                            aContext.failNow(ex);
                        }

                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public CompletableFuture<Void> subscribe(Set<TopicName> aTopics, SubscriberId aSubscriber) {
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public CompletableFuture<Void> unsubscribe(Set<TopicName> aTopics, SubscriberId aId) {
                        return CompletableFuture.completedFuture(null);
                    }
                };
        final ContentModerationService contentModService = contentModerationService();
        final ChannelApplicationService channelService = buildChannelAppService(messagePublisher, contentModService);

        channelService.open()
                .thenCompose(v -> publish(channelService, appId, topicId, message1, false, false, new UserId("userA")))
                .thenCompose(v -> publish(channelService, appId, topicId, message2, false, true, new UserId("userB")))
                .thenCompose(v -> publish(channelService, appId, topicId, message3, false, false, new UserId("userC")))
                .handle((v, aError) -> {
                    if (!Objects.isNull(aError)) {
                        aContext.failNow(aError);
                    }
                    for (int msgCheck: msgsCheck) {
                        Assertions.assertEquals(1, msgCheck);
                    }
                    aContext.completeNow();
                    return CompletableFuture.completedFuture(null);
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should send push notification for publish message")
    void messagePushNotificationEventValidationTest(final VertxTestContext aContext) throws InterruptedException {
        TopicId topicId = new TopicId(UUID.randomUUID().toString());
        ChannelMessage message1 = buildMessage(topicId, 1, "userA", "message 1", "data 1");
        ChannelMessage message2 = buildMessage(topicId, 2, "userB", "message 2", "data 2");
        ChannelMessage message3 = buildMessage(topicId, 3, "userA", "Filter 3", "data 3");

        final AbstractTopicMessagePublisher messagePublisher = buildMessagePublisher();
        final ContentModerationService contentModService = contentModerationService();
        final ChannelApplicationService channelService = buildChannelAppService(messagePublisher, contentModService);

        channelService.open()
        .thenCompose(v -> publish(channelService, appId, topicId, message1, false, false, new UserId("userA")))
        .thenCompose(v -> publish(channelService, appId, topicId, message2, false, true, new UserId("userB")))
        .thenCompose(v -> publish(channelService, appId, topicId, message3, false, true, new UserId("user_offline")))
        .handle((aMessages, aError) -> {
            Assertions.assertTrue(Objects.isNull(aError));
            Assertions.assertEquals(1, ((MockPushNotificationService) pushNotificationService).notifyList().size());
            aContext.completeNow();
            return CompletableFuture.completedFuture(null);
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should add the specified subscribers")
    void subscribersAddedTest(final VertxTestContext aContext) throws InterruptedException {
        final TopicId topicId = new TopicId(UUID.randomUUID().toString());
        final UserId userA = new UserId("userA");
        final UserId userB = new UserId("userB");
        final UserId userC = new UserId("userA");
        final Subscriber subscriber1 = new Subscriber(subscriberRepo.nextIdentity(), userA, LanguageCode.ENGLISH);
        final Subscriber subscriber2 = new Subscriber(subscriberRepo.nextIdentity(), userB, LanguageCode.FRENCH);
        final Subscriber subscriber3 = new Subscriber(subscriberRepo.nextIdentity(), userC, LanguageCode.FRENCH);

        final AbstractTopicMessagePublisher messagePublisher = buildMessagePublisher();
        final ContentModerationService contentModService = contentModerationService();
        final ChannelApplicationService channelService = buildChannelAppService(messagePublisher, contentModService);

        channelService.subscribe(
            new ChannelSubscribeCommand(
                new ChannelId(appId, topicId.value()),
                subscriber1.userId().value(),
                subscriber1.id().value(),
                subscriber1.locale().value()
            )
        )
        .thenCompose(
            v -> channelService.subscribe(
                new ChannelSubscribeCommand(
                    new ChannelId(appId, topicId.value()),
                    subscriber2.userId().value(),
                    subscriber2.id().value(),
                    subscriber2.locale().value()
                )
            )
        )
        .thenCompose(
            v -> channelService.subscribe(
                new ChannelSubscribeCommand(
                    new ChannelId(appId, topicId.value()),
                    subscriber3.userId().value(),
                    subscriber3.id().value(),
                    subscriber3.locale().value()
                )
            )
        )
        .thenCompose(v -> subscriptionService.fetchLocales(new ChannelId(appId, topicId.value())))
        .handle((aLocales, aError) -> {
            Assertions.assertEquals(aLocales.size(), 2);
            Assertions.assertTrue(aLocales.contains(LanguageCode.ENGLISH));
            Assertions.assertTrue(aLocales.contains(LanguageCode.FRENCH));

            aContext.completeNow();

            return CompletableFuture.completedFuture(null);
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should not create channel application service for invalid input")
    void invalidRoomServiceTest() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new ChannelApplicationService(
                    null,
                    buildMessagePublisher(),
                    defaultMessageRepo,
                    appService,
                    new TranslationService((texts, aFrom, aTo) -> null),
                    moderationService,
                    new MockAuthorizationService(),
                    subscriptionService,
                    presenceService,
                    userNotificationService,
                    pushNotificationService,
                    new RoleName("muted"),
                    new RoleName("banned")
            )
        );
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new ChannelApplicationService(
                    defaultEventBus,
                    null,
                    defaultMessageRepo,
                    appService,
                    new TranslationService((texts, aFrom, aTo) -> null),
                    moderationService,
                    new MockAuthorizationService(),
                    subscriptionService,
                    presenceService,
                    userNotificationService,
                    pushNotificationService,
                    new RoleName("muted"),
                    new RoleName("banned")
            )
        );
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new ChannelApplicationService(
                    defaultEventBus,
                    buildMessagePublisher(),
                    null,
                    appService,
                    new TranslationService((texts, aFrom, aTo) -> null),
                    moderationService,
                    new MockAuthorizationService(),
                    subscriptionService,
                    presenceService,
                    userNotificationService,
                    pushNotificationService,
                    new RoleName("muted"),
                    new RoleName("banned")
            )
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ChannelApplicationService(
                        defaultEventBus,
                        buildMessagePublisher(),
                        defaultMessageRepo,
                        null,
                        new TranslationService((texts, aFrom, aTo) -> null),
                        moderationService,
                        new MockAuthorizationService(),
                        subscriptionService,
                        presenceService,
                        userNotificationService,
                        pushNotificationService,
                        new RoleName("muted"),
                        new RoleName("banned")
                )
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ChannelApplicationService(
                        defaultEventBus,
                        buildMessagePublisher(),
                        defaultMessageRepo,
                        appService,
                        null,
                        moderationService,
                        new MockAuthorizationService(),
                        subscriptionService,
                        presenceService,
                        userNotificationService,
                        pushNotificationService,
                        new RoleName("muted"),
                        new RoleName("banned")
                )
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ChannelApplicationService(
                        defaultEventBus,
                        buildMessagePublisher(),
                        defaultMessageRepo,
                        appService,
                        new TranslationService((texts, aFrom, aTo) -> null),
                        null,
                        new MockAuthorizationService(),
                        subscriptionService,
                        presenceService,
                        userNotificationService,
                        pushNotificationService,
                        new RoleName("muted"),
                        new RoleName("banned")
                )
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ChannelApplicationService(
                        defaultEventBus,
                        buildMessagePublisher(),
                        defaultMessageRepo,
                        appService,
                        new TranslationService((texts, aFrom, aTo) -> null),
                        moderationService,
                        null,
                        subscriptionService,
                        presenceService,
                        userNotificationService,
                        pushNotificationService,
                        new RoleName("muted"),
                        new RoleName("banned")
                )
        );
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ChannelApplicationService(
                        defaultEventBus,
                        buildMessagePublisher(),
                        defaultMessageRepo,
                        appService,
                        new TranslationService((texts, aFrom, aTo) -> null),
                        moderationService,
                        new MockAuthorizationService(),
                        null,
                        presenceService,
                        userNotificationService,
                        pushNotificationService,
                        new RoleName("muted"),
                        new RoleName("banned")
                )
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ChannelApplicationService(
                        defaultEventBus,
                        buildMessagePublisher(),
                        defaultMessageRepo,
                        appService,
                        new TranslationService((texts, aFrom, aTo) -> null),
                        moderationService,
                        new MockAuthorizationService(),
                        subscriptionService,
                        null,
                        userNotificationService,
                        pushNotificationService,
                        new RoleName("muted"),
                        new RoleName("banned")
                )
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ChannelApplicationService(
                        defaultEventBus,
                        buildMessagePublisher(),
                        defaultMessageRepo,
                        appService,
                        new TranslationService((texts, aFrom, aTo) -> null),
                        moderationService,
                        new MockAuthorizationService(),
                        subscriptionService,
                        presenceService,
                        null,
                        pushNotificationService,
                        new RoleName("muted"),
                        new RoleName("banned")
                )
        );

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ChannelApplicationService(
                        defaultEventBus,
                        buildMessagePublisher(),
                        defaultMessageRepo,
                        appService,
                        new TranslationService((texts, aFrom, aTo) -> null),
                        moderationService,
                        new MockAuthorizationService(),
                        subscriptionService,
                        presenceService,
                        userNotificationService,
                        null,
                        new RoleName("muted"),
                        new RoleName("banned")
                )
        );
    }

    private boolean messageEquals(final ChannelMessage aLHS, final TopicMessage aRHS) {
        final JsonObject payload = new JsonObject(aRHS.data());
        final ChannelMessage rhs = new ChannelMessage(
            aRHS.id(),
            aRHS.from(),
            payload.getString("nick"),
            new ChannelId(appId, payload.getString("to")),
            aLHS.topic(),
            payload.getString("body"),
            payload.getString("data"),
            null,
            new Date(aRHS.occurredOn()),
                null
        );

        return messageEquals(aLHS, rhs);
    }

    private boolean messageEquals(final ChannelMessage aLHS, final ChannelMessage aRHS) {
        return aLHS.body().equals(aRHS.body()) &&
                aLHS.nick().equals(aRHS.nick()) &&
                aLHS.from().equals(aRHS.from()) &&
                aLHS.id() == aRHS.id() ;
    }

    private ContentModerationService contentModerationService() {
        return new MockContentModerationService(
                appService, vertx);
    }

    private ChannelMessage buildMessage(TopicId aTopicId, int aId, String aUserId, String aBody, String aData) {
        return new ChannelMessage(
                aId,
                new UserId(aUserId),
                aUserId,
                new ChannelId(appId, aTopicId.value()),
                aTopicId,
                aBody,
                aData,
                null,
                new Date(),
                null
        );
    }

    CompletableFuture<Void> publish(ChannelApplicationService aService,
                                    ApplicationId aAppId,
                                    TopicId aTopicId,
                                    ChannelMessage aMessage,
                                    boolean aTranslate,
                                    boolean aFilter,
                                    UserId notifyUser) {

        final ChannelMessagePublishCommand cmd = new ChannelMessagePublishCommand.Builder()
                .setChannelId(new ChannelId(aAppId, aTopicId.value()))
                .setAuthorId(aMessage.from().value())
                .setNick(aMessage.nick())
                .setBody(aMessage.body())
                .setData(aMessage.data())
                .setTranslate(aTranslate)
                .setFilter(aFilter)
                .setPersist(true)
                .setNotifyList(Collections.singleton(notifyUser))
                .build();

        return aService.publish(cmd);
    }

    ChannelApplicationService buildChannelAppService(AbstractTopicMessagePublisher aMessagePublisher,
                                                     ContentModerationService contentModService) {
        return new ChannelApplicationService(
                defaultEventBus,
                aMessagePublisher,
                defaultMessageRepo,
                appService,
                new TranslationService(new MockTranslationClient()),
                contentModService,
                new MockAuthorizationService(),
                subscriptionService,
                presenceService,
                userNotificationService,
                pushNotificationService,
                new RoleName("muted"),
                new RoleName("banned")
        );
    }

    AbstractTopicMessagePublisher buildMessagePublisher() {
        return new MockTopicMessagePublisher();
    }
}
