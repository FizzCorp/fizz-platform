package io.fizz.chat.application.channel;

import io.fizz.chatcommon.domain.ChannelId;
import io.fizz.common.domain.UserId;

import java.util.Set;

public class ChannelMessageDeleteCommand extends AbstractChannelCommand {
    private final UserId authorId;
    private final long messageId;
    private Set<UserId> notifyList;

    public ChannelMessageDeleteCommand(final ChannelId aChannelId,
                                       final String aAuthorId,
                                       final long aMessageId,
                                       final Set<UserId> aNotifyList) {
        super(aChannelId);

        authorId = new UserId(aAuthorId);
        messageId = aMessageId;
        notifyList = aNotifyList;
    }

    public UserId authorId() {
        return authorId;
    }

    public long messageId() {
        return messageId;
    }

    public Set<UserId> notifyList() {
        return notifyList;
    }
}
