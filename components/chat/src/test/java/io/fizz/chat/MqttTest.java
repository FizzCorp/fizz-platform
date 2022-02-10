package io.fizz.chat;

import io.fizz.chat.application.topic.TopicApplicationService;
import io.fizz.chat.domain.channel.ChannelMessageReceivedForDeletion;
import io.fizz.chat.domain.channel.ChannelMessageReceivedForPublish;
import io.fizz.chat.domain.channel.ChannelMessageReceivedForUpdate;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chat.infrastructure.services.MockSubscriptionService;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.subscriber.AbstractSubscriberRepository;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.pubsub.infrastructure.messaging.EMQXTopicMessagePublisher;
import io.fizz.chat.pubsub.infrastructure.serde.JsonTopicMessageSerde;
import io.fizz.chatcommon.domain.events.DomainEventBus;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.chatcommon.infrastructure.messaging.MockEventPublisher;
import io.fizz.chatcommon.infrastructure.messaging.kafka.KafkaEventPublisher;
import io.fizz.chatcommon.infrastructure.serde.JsonEventSerde;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Disabled
@ExtendWith(VertxExtension.class)
class MqttTest {
    private static final int TEST_TIMEOUT = 1500;
    private static ApplicationId appId;
    private static TopicId topicId;
    private static SubscriberId subscriber;

    private static Vertx vertx;
    private static DomainEventBus eventBus;
    private static TopicApplicationService messagingService;

    static {
        try {
            appId = new ApplicationId("644fbdeb-0e69-4b4b-9720-496c8e45184e");
            topicId = new TopicId("testChannel01");
        }
        catch (DomainErrorException err) {
            System.out.println(err.getMessage());
        }
    }

    @BeforeAll
    static void setUp(VertxTestContext aContext) {
        vertx = Vertx.vertx();

        final EMQXTopicMessagePublisher messagePublisher = new EMQXTopicMessagePublisher(vertx, new JsonTopicMessageSerde());

        try {
            final AbstractSubscriberRepository subscriberRepo = new MockSubscriptionService();

            eventBus = new DomainEventBus();
            eventBus.register(new MockEventPublisher(new JsonEventSerde(), new DomainEventType[]{
                    ChannelMessageReceivedForPublish.TYPE,
                    ChannelMessageReceivedForUpdate.TYPE,
                    ChannelMessageReceivedForDeletion.TYPE
            }));
            subscriber = subscriberRepo.nextIdentity();

            messagingService = new TopicApplicationService(eventBus, messagePublisher);
            aContext.completeNow();
        } catch (Exception ex) {
            aContext.failNow(ex);
        }
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        aContext.completeNow();
    }

    @Test
    void subscribeUser(VertxTestContext aContext) throws InterruptedException {
        messagingService.subscribe(appId.value(), topicId.value(), subscriber.value())
                .whenComplete((v, aError) -> {
                    Assertions.assertTrue(Objects.isNull(aError));
                    aContext.completeNow();
                });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    void publishMessage(VertxTestContext aContext) throws InterruptedException {
        messagingService.publish(
                appId.value(),
                topicId.value(),
                1L,
                "test",
                subscriber.value(),
                "hello there mqtt!!!"
            )
            .whenComplete((v, aError) -> {
                Assertions.assertTrue(Objects.isNull(aError));
                aContext.completeNow();
            });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }
}
