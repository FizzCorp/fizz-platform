package io.fizz.chat.domain.subscriber;

import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chat.pubsub.domain.subscriber.SubscriberId;
import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.chatcommon.domain.LanguageCode;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface AbstractSubscriptionService {
    CompletableFuture<TopicId> assignOrFetchTopic(final ChannelId aChannelId,
                                                  final SubscriberId aSubscriberId,
                                                  final LanguageCode aLocale);
    CompletableFuture<Set<LanguageCode>> fetchLocales(final ChannelId aChannelId);
    CompletableFuture<Set<TopicId>> fetchTopics(final ChannelId aChannelId);
    TopicId defaultTopic(final ChannelId aChannelId);
    CompletableFuture<TopicId> fetchTopic(final ChannelId aChannelId,
                                          final String aTopicId);
}
