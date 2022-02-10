package io.fizz.chat.group.domain.group;

import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.Set;

public class GroupMemberReadMessages extends GroupMemberEvent {
    public static final DomainEventType TYPE =
            new DomainEventType("groupMemberReadMsgs", GroupMemberReadMessages.class);

    private final Long lastReadMessageId;

    public GroupMemberReadMessages(final GroupMember aMember,
                                   final Set<UserId> aMemberIds,
                                   final Date aOccurredOn) {
        super(TYPE, aMember, aMemberIds, aOccurredOn);

        lastReadMessageId = aMember.lastReadMessageId();
    }

    public Long lastReadMessageId() {
        return lastReadMessageId;
    }
}
