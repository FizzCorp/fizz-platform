package io.fizz.chat.infrastructure.services;

import io.fizz.chat.pubsub.domain.subscriber.AbstractSubscriberRepository;
import io.fizz.chat.domain.subscriber.AbstractSubscriptionService;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MockSubscriptionService implements AbstractSubscriptionService, AbstractSubscriberRepository {
    private final Map<String,Set<LanguageCode>> locales = new HashMap<>();

    @Override
    public SubscriberId nextIdentity() {
        try {
            return new SubscriberId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        catch (Exception ex) {
            return null;
        }
    }

    @Override
    public CompletableFuture<TopicId> assignOrFetchTopic(final ChannelId aChannelId,
                                                         final SubscriberId aSubscriberId,
                                                         final LanguageCode aLocale) {
        Set<LanguageCode> channelLocales = locales.get(aChannelId.value());
        if (Objects.isNull(channelLocales)) {
            channelLocales = new HashSet<>();
            locales.put(aChannelId.value(), channelLocales);
        }
        channelLocales.add(aLocale);

        return CompletableFuture.completedFuture(new TopicId(aChannelId.value()));
    }

    @Override
    public CompletableFuture<Set<LanguageCode>> fetchLocales(ChannelId aChannelId) {
        final Set<LanguageCode> outLocales = locales.get(aChannelId.value());

        return CompletableFuture.completedFuture(Objects.nonNull(outLocales) ? outLocales : new HashSet<>());
    }

    @Override
    public CompletableFuture<Set<TopicId>> fetchTopics(ChannelId aChannelId) {
        Map<String, Set<TopicId>> channelTopics = new HashMap<>();
        try {
            Set<TopicId> topics = new HashSet<>();
            topics.add(new TopicId("topic1"));
            topics.add(new TopicId("topic2"));
            channelTopics.put(aChannelId.value(), topics);
        } catch (Exception ex) {

        }
        return CompletableFuture.completedFuture(channelTopics.get(aChannelId.value()));
    }

    @Override
    public TopicId defaultTopic(final ChannelId aChannelId) {
        return new TopicId(aChannelId.value());
    }

    @Override
    public CompletableFuture<TopicId> fetchTopic(ChannelId aChannelId, String aTopicId) {
        return CompletableFuture.completedFuture(new TopicId(aTopicId));
    }
}
