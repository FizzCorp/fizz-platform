package io.fizz.chat.moderation.domain;

import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.common.Utils;

public class ReportedChannel {
    private final ChannelId channelId;
    private final long count;

    public ReportedChannel(ChannelId channelId, long count) {
        Utils.assertRequiredArgument(channelId, "invalid_channel_id");
        Utils.assertArgumentRange(count, 0L, Long.MAX_VALUE, "invalid_count");
        this.channelId = channelId;
        this.count = count;
    }

    public ChannelId channelId() {
        return channelId;
    }

    public long count() {
        return count;
    }
}
