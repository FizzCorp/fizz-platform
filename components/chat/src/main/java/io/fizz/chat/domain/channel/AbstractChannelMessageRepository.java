package io.fizz.chat.domain.channel;

import io.fizz.common.domain.ApplicationId;
import io.fizz.chat.domain.topic.TopicId;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AbstractChannelMessageRepository {
    CompletableFuture<Long> nextMessageId(final ApplicationId aAppId, final TopicId aTopicId);
    CompletableFuture<Boolean> save(final ApplicationId aAppId, final TopicId aTopicId, final ChannelMessage aMessage);
    CompletableFuture<Boolean> update(final ApplicationId aAppId, final TopicId aTopicId, final ChannelMessage aMessage);
    CompletableFuture<Void> remove(final ApplicationId aAppId, final TopicId aTopicId, long aMessageId);
    CompletableFuture<List<ChannelMessage>> query(final ApplicationId aAppId,
                                                  final TopicId aTopicId,
                                                  int aCount,
                                                  Long aBefore,
                                                  Long aAfter);
    CompletableFuture<List<ChannelMessage>> queryLatest(final ApplicationId aAppId, final TopicId aTopicId, int aCount);
    CompletableFuture<ChannelMessage> messageOfId(final ApplicationId aAppId, final TopicId aTopicId, long aMessageId);
}
