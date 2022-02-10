package io.fizz.chat.infrastructure.persistence;

import io.fizz.chat.domain.channel.ChannelMessage;
import io.fizz.chat.domain.subscriber.AbstractSubscriptionService;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chat.infrastructure.services.HBaseUIDService;
import io.fizz.chat.infrastructure.services.MockSubscriptionService;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.client.hbase.client.AbstractHBaseClient;
import io.fizz.client.hbase.client.MockHBaseClient;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.DomainErrorException;
import io.fizz.common.domain.UserId;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
class HBaseChannelMessageRepositoryTest {
    private static final int TEST_TIMEOUT = 15;
    private static ApplicationId appId;

    private static Vertx vertx;
    private static HBaseChannelMessageRepository messageRepo;

    @BeforeAll
    static void setUp(VertxTestContext aContext) {
        final AbstractHBaseClient client = new MockHBaseClient();
        final HBaseUIDService idService = new HBaseUIDService(client);
        final AbstractSubscriptionService subscriptionService = new MockSubscriptionService();

        messageRepo = new HBaseChannelMessageRepository(client, idService, subscriptionService);
        vertx = Vertx.vertx();

        try {
            appId = new ApplicationId("testApp");
        }
        catch (DomainErrorException err) {
            System.out.println(err.getMessage());
        }

        aContext.completeNow();
    }

    @AfterAll
    static void tearDown(VertxTestContext aContext) {
        if (!Objects.isNull(vertx)) {
            vertx.close(res -> aContext.completeNow());
        }
        aContext.completeNow();
    }

    @Test
    @DisplayName("it should generate sortable message ids in ascending order")
    void messageIDGenerationTest(final VertxTestContext aContext) throws InterruptedException {
        final List<Long> ids = new ArrayList<>();
        final TopicId topicId = new TopicId(UUID.randomUUID().toString());

        messageRepo.nextMessageId(appId, topicId)
        .thenCompose(aId -> {
            ids.add(aId);
            return messageRepo.nextMessageId(appId, topicId);
        })
        .thenCompose(aId -> {
            ids.add(aId);
            return messageRepo.nextMessageId(appId, topicId);
        })
        .thenCompose(aId -> {
            ids.add(aId);
            return messageRepo.nextMessageId(appId, topicId);
        })
        .handle((aId, aError) -> {
            ids.add(aId);

            Assertions.assertTrue(Objects.isNull(aError));
            for (int ii = 1; ii < ids.size(); ii++) {
                Assertions.assertTrue(ids.get(ii) > ids.get(ii-1));
            }
            aContext.completeNow();
            return null;
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("it should fetch latest messages")
    void fetchLatestMessagesTet(final VertxTestContext aContext) throws InterruptedException {
        final TopicId topicId = new TopicId(UUID.randomUUID().toString());
        final TopicId topicId2 = new TopicId(UUID.randomUUID().toString());
        final ChannelMessage message1 = buildMessage(0L, "userA", topicId.value(),"message1", "data1");
        final ChannelMessage message2 = buildMessage(1L, "userB", topicId.value(), "message2", "data2");
        final ChannelMessage message3 = buildMessage(2L, "userA", topicId.value(), "message3", "data3");
        final ChannelMessage message4 = buildMessage(3L, "userB", topicId.value(),"message4", "data4");
        final ChannelMessage message5 = buildMessage(4L, "userC", topicId2.value(),"message5", "data5");

        message1.setTranslation(LanguageCode.SPANISH, "test");

        messageRepo.save(appId, topicId, message1)
        .thenCompose(v -> messageRepo.save(appId, topicId, message2))
        .thenCompose(v -> messageRepo.save(appId, topicId, message3))
        .thenCompose(v -> messageRepo.save(appId, topicId, message4))
        .thenCompose(v -> messageRepo.save(appId, topicId2, message5))
        .thenCompose(v -> messageRepo.queryLatest(appId, topicId, 2))
        .thenCompose(aMessages -> {
            Assertions.assertEquals(aMessages.size(), 2);
            Assertions.assertEquals(aMessages.get(0), message3);
            Assertions.assertEquals(aMessages.get(1), message4);
            return messageRepo.query(appId, topicId, 20, 2L, null);
        })
        .thenCompose(aMessages -> {
            Assertions.assertEquals(aMessages.size(), 2);
            Assertions.assertEquals(aMessages.get(0), message1);
            Assertions.assertEquals(aMessages.get(1), message2);
            Assertions.assertEquals(aMessages.get(0).translations().get(LanguageCode.SPANISH), "test");
            return messageRepo.query(appId, topicId, 20, null, 2L);
        })
        .thenCompose(aMessages -> {
            Assertions.assertEquals(aMessages.size(), 1);
            Assertions.assertEquals(aMessages.get(0), message4);
            return messageRepo.queryLatest(appId, topicId, 50);
        })
        .handle((aMessages, aError) -> {
            Assertions.assertEquals(aMessages.size(), 4);
            Assertions.assertEquals(aMessages.get(0), message1);
            Assertions.assertEquals(aMessages.get(1), message2);
            Assertions.assertEquals(aMessages.get(2), message3);
            Assertions.assertEquals(aMessages.get(3), message4);
            aContext.completeNow();
            return null;
        });

        Assertions.assertTrue(aContext.awaitCompletion(TEST_TIMEOUT, TimeUnit.SECONDS));
    }

    private ChannelMessage buildMessage(Long aId, String aFrom, String aTo, String aBody, String aData) {
        return new ChannelMessage(
            aId,
            new UserId("test"),
            aFrom,
            new ChannelId(appId, aTo),
            new TopicId(aTo),
            aBody,
            aData,
            null,
            new Date(),
            null
        );
    }
}
