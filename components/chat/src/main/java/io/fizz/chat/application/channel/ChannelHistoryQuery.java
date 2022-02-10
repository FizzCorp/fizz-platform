package io.fizz.chat.application.channel;

import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.common.domain.UserId;

public class ChannelHistoryQuery extends AbstractChannelCommand{
    private final UserId requesterId;
    private final int count;
    private final Long before;
    private final Long after;

    public ChannelHistoryQuery(final ChannelId aChannelId,
                               final String aRequesterId,
                               int aCount,
                               Long aBefore,
                               Long aAfter) {
        super(aChannelId);

        requesterId = new UserId(aRequesterId);
        count = aCount;
        before = aBefore;
        after = aAfter;
    }

    public UserId requesterId() {
        return requesterId;
    }

    public int count() {
        return count;
    }

    public Long before() {
        return before;
    }

    public Long after() {
        return after;
    }
}
