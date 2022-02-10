package io.fizz.chat.group.domain.group;

import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.common.domain.UserId;

import java.util.*;

public class GroupMemberAdded extends GroupMemberEvent {
    public static final DomainEventType TYPE = new DomainEventType("groupMemberAdded", GroupMemberAdded.class);

    public GroupMemberAdded(final GroupMember aMember, final Set<UserId> aMemberIds, final Date aOccurredOn) {
        super(TYPE, aMember, aMemberIds, aOccurredOn);
    }
}
