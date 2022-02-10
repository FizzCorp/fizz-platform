package io.fizz.chat.domain.channel;

import io.fizz.chat.domain.topic.TopicId;
import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.Set;

public abstract class AbstractChannelMessageEvent implements AbstractDomainEvent {
    private static final IllegalArgumentException ERROR_INVALID_APP_ID = new IllegalArgumentException("invalid_app_id");
    private static final IllegalArgumentException ERROR_INVALID_TIME = new IllegalArgumentException("invalid_event_time");
    private static final IllegalArgumentException ERROR_INVALID_MESSAGE = new IllegalArgumentException("invalid_message");
    private static final IllegalArgumentException ERROR_INVALID_NOTIFY_LIST = new IllegalArgumentException("invalid_notify_list");

    private final ApplicationId appId;
    private final TopicId topicId;
    private final ChannelMessage message;
    private final boolean translate;
    private final boolean filter;
    private final long occurredOn;
    private final Set<UserId> notifyList;

    public AbstractChannelMessageEvent(final ApplicationId aAppId,
                                       final TopicId aTopicId,
                                       final ChannelMessage aMessage,
                                       final boolean aTranslate,
                                       final boolean aFilter,
                                       final Date aOccurredOn,
                                       final Set<UserId> aNotifyList) {
        Utils.assertRequiredArgument(aAppId, ERROR_INVALID_APP_ID);
        Utils.assertRequiredArgument(aTopicId, TopicId.ERROR_INVALID_TOPIC_ID);
        Utils.assertRequiredArgument(aMessage, ERROR_INVALID_MESSAGE);
        Utils.assertRequiredArgument(aOccurredOn, ERROR_INVALID_TIME);
        Utils.assertRequiredArgument(aOccurredOn, ERROR_INVALID_NOTIFY_LIST);

        appId = aAppId;
        topicId = aTopicId;
        message = aMessage;
        occurredOn = aOccurredOn.getTime();
        translate = aTranslate;
        filter = aFilter;
        notifyList = aNotifyList;
    }

    @Override
    public Date occurredOn() {
        return new Date(occurredOn);
    }

    public ApplicationId appId() {
        return appId;
    }

    public TopicId topicId() {
        return topicId;
    }

    public ChannelMessage message() {
        return message;
    }

    public boolean translate() {
        return translate;
    }

    public boolean filter() {
        return filter;
    }

    public Set<UserId> notifyList() {
        return notifyList;
    }
}
