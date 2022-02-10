package io.fizz.chat.domain.channel;

import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class ChannelMessageReceivedForPublish extends AbstractChannelMessageEvent {
    public static final DomainEventType TYPE = new DomainEventType("recvMsgForPublish", ChannelMessageReceivedForPublish.class);

    private final boolean persist;
    private final boolean internal;

    public ChannelMessageReceivedForPublish(final ApplicationId aAppId,
                                            final TopicId aTopicId,
                                            final ChannelMessage aMessage,
                                            final boolean aTranslate,
                                            final boolean aFilter,
                                            final boolean aPersist,
                                            final Date aOccurredOn,
                                            final Set<UserId> aNotifyList,
                                            final boolean aInternal) {
        super(aAppId, aTopicId, aMessage, aTranslate, aFilter, aOccurredOn, aNotifyList);

        persist = aPersist;
        internal = aInternal;
    }

    @Override
    public DomainEventType type() {
        return TYPE;
    }

    public boolean persist() {
        return persist;
    }

    public boolean isInternal() {
        return internal;
    }
}
