package io.fizz.chat.application.channel;

import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;

public abstract class AbstractChannelCommand {
    protected final ChannelId channelId;

    AbstractChannelCommand(final ChannelId aChannelId) {
        Utils.assertRequiredArgument(aChannelId, "invalid_channel_id");
        channelId = aChannelId;
    }

    public ApplicationId appId() {
        return channelId.appId();
    }

    public ChannelId channelId() {
        return channelId;
    }
}
