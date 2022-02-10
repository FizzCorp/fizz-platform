package io.fizz.chat.infrastructure.bootstrap;

import io.fizz.chat.domain.channel.ChannelMessageReceivedForDeletion;
import io.fizz.chat.domain.channel.ChannelMessageReceivedForPublish;
import io.fizz.chat.domain.channel.ChannelMessageReceivedForUpdate;
import io.fizz.chat.domain.channel.ChannelMessageTranslated;
import io.fizz.chat.infrastructure.services.MockSubscriptionService;
import io.fizz.chat.pubsub.infrastructure.messaging.MockTopicMessagePublisher;
import io.fizz.chatcommon.domain.RedisNamespace;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.chatcommon.infrastructure.VertxRedisClientProxy;
import io.fizz.chatcommon.infrastructure.messaging.MockEventPublisher;
import io.fizz.chatcommon.infrastructure.serde.JsonEventSerde;
import io.fizz.common.LoggingService;
import io.vertx.core.Vertx;

import java.util.concurrent.CompletableFuture;

public class MockChatComponent extends ChatComponent {
    private static final LoggingService.Log logger = LoggingService.getLogger(MockChatComponent.class);

    @Override
    protected CompletableFuture<Void> createEventPublisher(Vertx aVertx, String aKafkaServers) {
        logger.info("creating chat publishers...");

        eventPublisher = new MockEventPublisher(new JsonEventSerde(), new DomainEventType[]{
                ChannelMessageReceivedForPublish.TYPE,
                ChannelMessageReceivedForUpdate.TYPE,
                ChannelMessageReceivedForDeletion.TYPE,
                ChannelMessageTranslated.TYPE
        });

        eventBus.register(eventPublisher);

        return CompletableFuture.completedFuture(null);
    }

    @Override
    protected CompletableFuture<Void> createSubscriptionService(final VertxRedisClientProxy aRedisClient,
                                                                final RedisNamespace aRedisNamespace,
                                                                final int aSubscriberTTL) {
        final MockSubscriptionService service = new MockSubscriptionService();

        subscriptionService = service;
        subscriberRepo = service;

        return CompletableFuture.completedFuture(null);
    }
}
