package io.fizz.chat.infrastructure.services;

import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.chatcommon.domain.RedisNamespace;
import io.fizz.chatcommon.infrastructure.VertxRedisClientProxy;
import io.fizz.common.domain.ApplicationId;
import io.vertx.junit5.VertxExtension;
import io.vertx.redis.client.Redis;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@ExtendWith(VertxExtension.class)
class RedisSubscriptionServiceTest {
    private static VertxRedisClientProxy client = null;
    private static ApplicationId app;

    @Test
    @Disabled
    @DisplayName("it should not create channel id with $ character")
    void channelIdTest() {
        Assertions.assertThrows(
                IllegalArgumentException.class, () -> new ChannelId(new ApplicationId("appA"), "te$t")
        );
    }

    // Uncomment block to before running tests
    /*@BeforeAll
    @Disabled
    static void setUp(VertxTestContext aContext) throws DomainErrorException, InterruptedException {
        Redis.createClient(Vertx.vertx(), new RedisOptions().setEndpoint(SocketAddress.inetSocketAddress(6379, "localhost")))
                .connect(aResult -> {
                    if (aResult.succeeded()) {
                        client = aResult.result();
                        aContext.completeNow();
                    }
                    else {
                        aContext.failNow(aResult.cause());
                    }
                });
        app = new ApplicationId("appA");

        Assertions.assertTrue(aContext.awaitCompletion(10, TimeUnit.SECONDS));
    }*/

    @Test
    @Disabled
    void basicAssignmentTest() throws InterruptedException, ExecutionException {
        final RedisSubscriptionService service = new RedisSubscriptionService(client, RedisNamespace.CHAT, 10, 100);
        final ChannelId channel = new ChannelId(app, UUID.randomUUID().toString());

        service.open().get();

        final SubscriberId subA = service.nextIdentity();
        final TopicId topicA = service.assignOrFetchTopic(channel, subA, LanguageCode.ENGLISH).get();
        final SubscriberId subB = service.nextIdentity();
        final TopicId topicB = service.assignOrFetchTopic(channel, subB, LanguageCode.FRENCH).get();

        Assertions.assertEquals(topicA, topicB);

        final Set<LanguageCode> locales = service.fetchLocales(channel).get();
        Assertions.assertEquals(locales.size(), 2);
        Assertions.assertTrue(locales.contains(LanguageCode.ENGLISH));
        Assertions.assertTrue(locales.contains(LanguageCode.FRENCH));
    }

    @Test
    @Disabled
    void reassignmentTest() throws InterruptedException, ExecutionException {
        final RedisSubscriptionService service = new RedisSubscriptionService(client, RedisNamespace.CHAT, 10, 100);
        final ChannelId channel = new ChannelId(app, UUID.randomUUID().toString());

        service.open().get();

        final SubscriberId subA = service.nextIdentity();
        final TopicId topicA = service.assignOrFetchTopic(channel, subA, LanguageCode.ENGLISH).get();
        final TopicId topicB = service.assignOrFetchTopic(channel, subA, LanguageCode.FRENCH).get();

        Assertions.assertEquals(topicA, topicB);

        final Set<LanguageCode> locales = service.fetchLocales(channel).get();
        Assertions.assertEquals(locales.size(), 1);
        Assertions.assertTrue(locales.contains(LanguageCode.ENGLISH));
    }

    @Test
    @Disabled
    void scalingTest() throws InterruptedException, ExecutionException {
        final int ttl = 10;
        final int topicSize = 10;
        final int totalSubscribers = 50;
        final int topicCount = 5;

        final RedisSubscriptionService service = new RedisSubscriptionService(client, RedisNamespace.CHAT, topicSize, ttl);
        final ChannelId channel = new ChannelId(app, UUID.randomUUID().toString());

        service.open().get();

        Set<String> topics = new HashSet<>();
        for (int si = 0; si < totalSubscribers; si++) {
            final TopicId topic = service.assignOrFetchTopic(channel, service.nextIdentity(), LanguageCode.ENGLISH).get();
            topics.add(topic.value());
        }

        Assertions.assertEquals(topicCount, topics.size());
    }
}
