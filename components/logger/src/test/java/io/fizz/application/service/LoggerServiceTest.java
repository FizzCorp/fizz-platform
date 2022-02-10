package io.fizz.application.service;

import io.fizz.chat.application.AbstractApplicationRepository;
import io.fizz.chat.application.UserNotificationApplicationService;
import io.fizz.chat.application.channel.ChannelApplicationService;
import io.fizz.chat.application.impl.ApplicationService;
import io.fizz.chat.application.impl.HBaseApplicationRepository;
import io.fizz.chat.application.impl.MockApplicationService;
import io.fizz.chat.application.services.TranslationService;
import io.fizz.chat.domain.channel.*;
import io.fizz.chat.infrastructure.persistence.HBaseChannelMessageRepository;
import io.fizz.chat.infrastructure.services.HBaseUIDService;
import io.fizz.chat.infrastructure.services.MockPushNotificationService;
import io.fizz.chat.infrastructure.services.MockSubscriptionService;
import io.fizz.chat.infrastructure.services.MockTranslationClient;
import io.fizz.chat.moderation.application.service.ContentModerationService;
import io.fizz.chat.moderation.application.service.MockContentModerationService;
import io.fizz.chat.presence.infrastructure.presence.MockUserPresenceService;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.infrastructure.messaging.MockTopicMessagePublisher;
import io.fizz.chataccess.domain.MockAuthorizationService;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chatcommon.domain.events.DomainEventBus;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.chatcommon.infrastructure.messaging.MockEventPublisher;
import io.fizz.chatcommon.infrastructure.serde.JsonEventSerde;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.client.MockHBaseClient;
import io.fizz.logger.application.service.LoggerService;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
class LoggerServiceTest {
    private static final int TEST_TIMEOUT = 5;
    private static DomainEventBus defaultEventBus;
    private static ApplicationService appService;
    private static HBaseChannelMessageRepository defaultMessageRepo;

    private static Vertx vertx;

    @BeforeAll
    static void setUp(VertxTestContext aContext) {
        vertx = Vertx.vertx();

        final AbstractHBaseClient defaultClient = new MockHBaseClient();
        final HBaseUIDService defaultIdService = new HBaseUIDService(defaultClient);
        final AbstractApplicationRepository appRepo = new HBaseApplicationRepository(defaultClient);

        appService = new MockApplicationService(appRepo);
        defaultEventBus = new DomainEventBus();
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
        aContext.completeNow();
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        vertx.close(res -> aContext.completeNow());
    }

    @Test
    @DisplayName("it should return logs in the same order they were published")
    void writeLogsValidationTest(final VertxTestContext aContext) throws InterruptedException {
        final AbstractTopicMessagePublisher messagePublisher = buildMessagePublisher();
        final ContentModerationService contentModService = contentModerationService();
        final ChannelApplicationService channelService = buildChannelAppService(messagePublisher, contentModService);
        final LoggerService loggerService = buildLoggerService(channelService);

        channelService.open()
        .thenCompose(v -> loggerService.write("testApp", "LogChannel1", "data 1"))
        .thenCompose(v -> loggerService.write("testApp", "LogChannel1", "data 2"))
        .thenCompose(v -> loggerService.write("testApp", "LogChannel1", "data 3"))
        .thenCompose(v -> loggerService.read("testApp","LogChannel1", 3))
        .handle((aLogs, aError) -> {
            Assertions.assertTrue(Objects.isNull(aError));
            Assertions.assertEquals(3, aLogs.size());
            Assertions.assertTrue(logEquals("LogChannel1", "data 1", aLogs.get(0)));
            Assertions.assertTrue(logEquals("LogChannel1", "data 2", aLogs.get(1)));
            Assertions.assertTrue(logEquals("LogChannel1", "data 3", aLogs.get(2)));

            aContext.completeNow();
            return CompletableFuture.completedFuture(null);
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should return logs as per count")
    void readLogsCountValidationTest(final VertxTestContext aContext) throws InterruptedException {
        final AbstractTopicMessagePublisher messagePublisher = buildMessagePublisher();
        final ContentModerationService contentModService = contentModerationService();
        final ChannelApplicationService channelService = buildChannelAppService(messagePublisher, contentModService);
        final LoggerService loggerService = buildLoggerService(channelService);

        channelService.open()
            .thenCompose(v -> loggerService.write("testApp", "LogChannel1", "data 1"))
            .thenCompose(v -> loggerService.write("testApp", "LogChannel1", "data 2"))
            .thenCompose(v -> loggerService.write("testApp", "LogChannel1", "data 3"))
            .thenCompose(v -> loggerService.write("testApp", "LogChannel1", "data 4"))
            .thenCompose(v -> loggerService.read("testApp","LogChannel1", 2))
            .handle((aLogs, aError) -> {
                Assertions.assertTrue(Objects.isNull(aError));
                Assertions.assertEquals(2, aLogs.size());
                Assertions.assertTrue(logEquals("LogChannel1", "data 3", aLogs.get(0)));
                Assertions.assertTrue(logEquals("LogChannel1", "data 4", aLogs.get(1)));

                aContext.completeNow();
                return CompletableFuture.completedFuture(null);
            });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should throw exception for invalid log counts")
    void readLogCountBoundaryTest(final VertxTestContext aContext) throws InterruptedException {

        final AbstractTopicMessagePublisher messagePublisher = buildMessagePublisher();
        final ContentModerationService contentModService = contentModerationService();
        final ChannelApplicationService channelService = buildChannelAppService(messagePublisher, contentModService);
        final LoggerService loggerService = buildLoggerService(channelService);

        channelService.open()
            .thenApply(v -> {

                Assertions.assertThrows(IllegalArgumentException.class, () -> loggerService.read("testApp","LogChannel1", -1));
                Assertions.assertThrows(IllegalArgumentException.class, () -> loggerService.read("testApp","LogChannel1", 51));

                aContext.completeNow();
                return CompletableFuture.completedFuture(null);
            });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should return maximum possible logs if count is Null")
    void readLogCountNullTest(final VertxTestContext aContext) throws InterruptedException {
        final AbstractTopicMessagePublisher topicService = buildMessagePublisher();
        final ContentModerationService contentModService = contentModerationService();
        final ChannelApplicationService channelService = buildChannelAppService(buildMessagePublisher(), contentModService);
        final LoggerService loggerService = buildLoggerService(channelService);

        channelService.open()
            .thenCompose(v -> writeLogs(loggerService, 55))
            .thenApply(v -> {
                loggerService.read("testApp","LogChannel1", null)
                        .handle((aLogs, aError) -> {
                            Assertions.assertTrue(Objects.isNull(aError));
                            Assertions.assertEquals(50, aLogs.size());

                            aContext.completeNow();
                            return CompletableFuture.completedFuture(null);
                        });

                return CompletableFuture.completedFuture(null);
            });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    private CompletableFuture<Void> writeLogs(final LoggerService loggerService, final int totalLogs) {
        if (totalLogs == 0) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Void> future = loggerService.write("testApp", "LogChannel1", "data " + totalLogs);
        future.thenCompose(v -> writeLogs(loggerService, totalLogs-1));
        return future;
    }


    private boolean logEquals(final String aLogId, final String aLogBody, final ChannelMessage aLog) {
        return aLogId.equals(aLog.to().value()) &&
                aLogBody.equals(aLog.body());
    }

    private AbstractTopicMessagePublisher buildMessagePublisher() {
        return new MockTopicMessagePublisher();
    }

    private ContentModerationService contentModerationService() {
        return new MockContentModerationService(appService, vertx);
    }

    ChannelApplicationService buildChannelAppService(AbstractTopicMessagePublisher aMessagePublisher,
                                                     ContentModerationService contentModService) {
        final AbstractTopicMessagePublisher publisher = new MockTopicMessagePublisher();
        final UserNotificationApplicationService notificationService = new UserNotificationApplicationService(publisher);
        return new ChannelApplicationService(
                defaultEventBus,
                aMessagePublisher,
                defaultMessageRepo,
                appService,
                new TranslationService(new MockTranslationClient()),
                contentModService,
                new MockAuthorizationService(),
                new MockSubscriptionService(),
                new MockUserPresenceService(),
                notificationService,
                new MockPushNotificationService(),
                new RoleName("muted"),
                new RoleName("banned")
        );
    }

    LoggerService buildLoggerService(ChannelApplicationService aChannelService) {
        return new LoggerService(aChannelService);
    }
}
