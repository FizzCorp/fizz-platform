package io.fizz.chat.domain.channel;

import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.common.domain.ApplicationId;
import io.fizz.chat.domain.topic.TopicId;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class ChannelMessageReceivedForDeletion extends AbstractChannelMessageEvent {
    public static final DomainEventType TYPE = new DomainEventType("recvMsgForDeletion", ChannelMessageReceivedForDeletion.class);

    public ChannelMessageReceivedForDeletion(final ApplicationId aAppId,
                                             final TopicId aTopicId,
                                             final ChannelMessage aMessage,
                                             final Date aOccurredOn,
                                             final Set<UserId> aNotifyList) {
        super(aAppId, aTopicId, aMessage, false, false, aOccurredOn, aNotifyList);
    }

    @Override
    public DomainEventType type() {
        return TYPE;
    }
}
