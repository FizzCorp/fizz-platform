package io.fizz.chat.application.topic;

import io.fizz.chat.domain.channel.ChannelMessageReceivedForDeletion;
import io.fizz.chat.domain.channel.ChannelMessageReceivedForUpdate;
import io.fizz.chat.pubsub.application.AbstractTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chat.pubsub.domain.topic.TopicMessage;
import io.fizz.chat.domain.channel.ChannelMessageReceivedForPublish;
import io.fizz.chat.pubsub.infrastructure.messaging.MockTopicMessagePublisher;
import io.fizz.chat.pubsub.domain.topic.TopicName;
import io.fizz.chatcommon.domain.events.DomainEventBus;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.chatcommon.infrastructure.messaging.MockEventPublisher;
import io.fizz.chatcommon.infrastructure.serde.JsonEventSerde;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
class TopicApplicationServiceTest {
    private static final int TEST_TIMEOUT = 5;
    private static ApplicationId appId;
    private static TopicId topicId;

    private static Vertx vertx;
    private static DomainEventBus eventBus;
    private static MockTopicMessagePublisher messagePublisher;

    @BeforeAll
    static void setUp(VertxTestContext aContext) {
        eventBus = new DomainEventBus();
        final MockEventPublisher publisher = new MockEventPublisher(new JsonEventSerde(), new DomainEventType[]{
                ChannelMessageReceivedForPublish.TYPE,
                ChannelMessageReceivedForUpdate.TYPE,
                ChannelMessageReceivedForDeletion.TYPE
        });

        eventBus.register(publisher);
        messagePublisher = new MockTopicMessagePublisher();
        vertx = Vertx.vertx();

        try {
            appId = new ApplicationId("testApp");
            topicId = new TopicId("testChannel01");
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
    void messagePublishValidationTest(final VertxTestContext aContext) throws InterruptedException {
        final String message1 = buildPayload("message1");
        final String message2 = buildPayload("message2");
        final String message3 = buildPayload("message3");

        final Checkpoint cp1 = aContext.checkpoint();
        final Checkpoint cp2 = aContext.checkpoint();
        final Checkpoint cp3 = aContext.checkpoint();

        final TopicApplicationService service = new TopicApplicationService(
                eventBus,
                new AbstractTopicMessagePublisher() {
                    @Override
                    public CompletableFuture<Void> publish(Set<TopicName> aTopics, TopicMessage aMessage) {
                        if (aMessage.data().equals(message1)) {
                            cp1.flag();
                            return CompletableFuture.completedFuture(null);
                        }
                        else
                        if (aMessage.data().equals(message2)) {
                            cp2.flag();
                            return CompletableFuture.completedFuture(null);
                        }
                        else
                        if (aMessage.data().equals(message3)) {
                            cp3.flag();
                            return CompletableFuture.completedFuture(null);
                        }
                        else {
                            return Utils.failedFuture(new IllegalStateException("invalid message"));
                        }
                    }

                    @Override
                    public CompletableFuture<Void> subscribe(Set<TopicName> aTopics, SubscriberId aSubscriber) {
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public CompletableFuture<Void> unsubscribe(Set<TopicName> aTopics, SubscriberId aId) {
                        return CompletableFuture.completedFuture(null);
                    }
                }
        );

        service.publish(appId.value(), topicId.value(), 1L, "test","userA", message1)
        .thenCompose(v -> service.publish(appId.value(), topicId.value(), 2L, "test", "userB", message2))
        .thenCompose(v -> service.publish(appId.value(), topicId.value(), 3L, "test","userA", message3))
        .handle((aMessages, aError) -> {
            Assertions.assertTrue(Objects.isNull(aError));
            return null;
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should not create messaging service for an invalid event bus")
    void messagingServiceInvalidCreationEventBusTest() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new TopicApplicationService(null, messagePublisher)
        );
    }

    @Test
    @DisplayName("it should not create messaging service for an invalid message publisher")
    void messagingServiceInvalidCreationMessagePublisherTest() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new TopicApplicationService(eventBus, null)
        );
    }

    private String buildPayload(final String aMessage) {
        return new JsonObject()
                .put("content", aMessage)
                .toString();
    }
}
