package io.fizz.chat.infrastructure.services;

import io.fizz.chat.pubsub.domain.subscriber.AbstractSubscriberRepository;
import io.fizz.chat.domain.subscriber.AbstractSubscriptionService;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;
import io.fizz.chatcommon.domain.RedisNamespace;
import io.fizz.chatcommon.infrastructure.VertxRedisClientProxy;
import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.vertx.redis.client.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RedisSubscriptionService implements AbstractSubscriptionService, AbstractSubscriberRepository {
    private static final char TOPIC_KEY_SEPARATOR = '$';
    private static final String TOPIC_SUB_KEY_SUFFIX = ":sub";

    private static final LoggingService.Log logger = LoggingService.getLogger(RedisSubscriptionService.class);

    private VertxRedisClientProxy client;
    private static int MAX_TOPICS = 64;

    private final RedisNamespace redisNamespace;
    private final int maxTopicSize;
    private final int ttlSeconds;
    private String scriptSHA;

    public RedisSubscriptionService(final VertxRedisClientProxy aClient,
                                    final RedisNamespace aRedisNamespace,
                                    final int aMaxTopicSize,
                                    final int aTTLSeconds) {
        Utils.assertRequiredArgument(aClient, "invalid redis client specified");
        Utils.assertRequiredArgument(aRedisNamespace, "invalid redis namespace specified");
        Utils.assertArgumentRange(
                aMaxTopicSize, 0, 10000, "invalid topic size specified"
        );

        if (aTTLSeconds < 0) {
            throw new IllegalArgumentException("invalid subscription TTL specified");
        }

        client = aClient;
        redisNamespace = aRedisNamespace;
        maxTopicSize = aMaxTopicSize;
        ttlSeconds = aTTLSeconds;
    }

    public CompletableFuture<Void> open() {
        try {
            final CompletableFuture<Void> opened = new CompletableFuture<>();
            final String script = Utils.loadFileAsResource("/redis_channel.lua");

            client.send(Request.cmd(Command.SCRIPT).arg("LOAD").arg(script))
                    .handle(((response, error) -> {
                        if (Objects.isNull(error)) {
                            scriptSHA = response.toString();
                            opened.complete(null);
                        }
                        else {
                            opened.completeExceptionally(error);
                        }
                        return null;
                    }));

            return opened;
        } catch (IOException ex) {
            return Utils.failedFuture(ex);
        }
    }

    @Override
    public SubscriberId nextIdentity() {
        try {
            return new SubscriberId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        catch (Exception ex) {
            logger.fatal("Invalid subscriber ID should not be generated.");
            return null;
        }
    }

    @Override
    public CompletableFuture<TopicId> assignOrFetchTopic(final ChannelId aChannelId,
                                                         final SubscriberId aSubscriberId,
                                                         final LanguageCode aLocale) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aChannelId, "invalid_channel_id");
            Utils.assertRequiredArgument(aSubscriberId, "invalid_subscriber_id");
            Utils.assertRequiredArgument(aLocale, "invalid_locale");

            final Request req = Request.cmd(Command.EVALSHA)
                    .arg(scriptSHA)
                    .arg(3)
                    .arg(redisNamespace.value())
                    .arg(aChannelId.appId().value())
                    .arg(aChannelId.value())
                    .arg("ASSIGN")
                    .arg(aSubscriberId.value())
                    .arg(aLocale.value())
                    .arg(ttlSeconds)
                    .arg(maxTopicSize);

            final CompletableFuture<TopicId> fetched = new CompletableFuture<>();
            client.send(req)
                    .handle(((response, error) -> {
                        if (Objects.isNull(error)) {
                            final TopicId topic = new TopicId(response.toString());
                            fetched.complete(topic);
                        }
                        else {
                            fetched.completeExceptionally(error);
                        }
                        return null;
                    }));

            return fetched;
        });
    }

    @Override
    public CompletableFuture<Set<LanguageCode>> fetchLocales(final ChannelId aChannelId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aChannelId, "invalid_channel_id");

            final Request req = Request.cmd(Command.EVALSHA)
                    .arg(scriptSHA)
                    .arg(3)
                    .arg(redisNamespace.value())
                    .arg(aChannelId.appId().value())
                    .arg(aChannelId.value())
                    .arg("LOCALES");

            final CompletableFuture<Set<LanguageCode>> fetched = new CompletableFuture<>();
            client.send(req)
                    .handle(((response, error) -> {
                        if (Objects.isNull(error)) {
                            final Set<LanguageCode> locales = new HashSet<>();

                            for (Response item: response) {
                                try {
                                    locales.add(LanguageCode.fromValue(item.toString()));
                                }
                                catch (Exception ex) {
                                    logger.fatal("invalid locale fetched");
                                }
                            }
                            fetched.complete(locales);
                        }
                        else {
                            fetched.completeExceptionally(error);
                        }
                        return null;
                    }));

            return fetched;
        });
    }

    @Override
    public CompletableFuture<Set<TopicId>> fetchTopics(final ChannelId aChannelId) {
        return Utils.async(() -> {
            Utils.assertRequiredArgument(aChannelId, "invalid_channel_id");

            return fetchTopicCount(aChannelId)
                    .thenApply(aTopicCount -> {
                        final Set<TopicId> topics = new HashSet<>();

                        for (int si = 0; si < aTopicCount; si++) {
                            topics.add(buildTopicId(aChannelId, si));
                        }

                        topics.add(defaultTopic(aChannelId));

                        return topics;
                    });
        });
    }

    @Override
    public TopicId defaultTopic(final ChannelId aChannelId) {
        Utils.assertRequiredArgument(aChannelId, "invalid_channel_id");

        return new TopicId(aChannelId.value());
    }

    @Override
    public CompletableFuture<TopicId> fetchTopic(ChannelId aChannelId, String aTopicId) {
        return Utils.async(() -> {
            final TopicId topicId = new TopicId(aTopicId);

            return fetchTopics(aChannelId)
                    .thenApply(aTopicIds -> aTopicIds.contains(topicId) ? topicId : null);
        });
    }

    private CompletableFuture<Integer> fetchTopicCount(final ChannelId aChannelId) {
        final Request req = Request.cmd(Command.EVALSHA)
                .arg(scriptSHA)
                .arg(3)
                .arg(redisNamespace.value())
                .arg(aChannelId.appId().value())
                .arg(aChannelId.value())
                .arg("TOPIC_COUNT");

        final CompletableFuture<Integer> fetched = new CompletableFuture<>();
        client.send(req)
                .handle(((response, error) -> {
                    if (Objects.isNull(error)) {
                        final Integer topicCount = response.toInteger();
                        fetched.complete(topicCount);
                    }
                    else {
                        fetched.completeExceptionally(error);
                    }
                    return null;
                }));
        return fetched;
    }

    private TopicId buildTopicId(final ChannelId aChannelId, final int aTopicSeq) {
        String value;

        if (aTopicSeq <= 0) {
            value = aChannelId.value();
        }
        else {
            value = aChannelId.value() + TOPIC_KEY_SEPARATOR + aTopicSeq;
        }

        return new TopicId(value);
    }
}
