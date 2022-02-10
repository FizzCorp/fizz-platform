package io.fizz.chat.group.domain.group;

import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.common.domain.UserId;

import java.util.Date;

import java.util.Set;

public abstract class GroupMemberEvent implements AbstractDomainEvent {
    private final DomainEventType eventType;
    private final GroupId groupId;
    private final UserId memberId;
    private final GroupMember.State state;
    private final RoleName role;
    private final Date createdOn;
    private final Set<UserId> memberIds;
    private final Date occurredOn;

    public GroupMemberEvent(final DomainEventType aType,
                            final GroupMember aMember,
                            final Set<UserId> aMemberIds,
                            final Date aOccurredOn) {
        this.eventType = aType;
        this.groupId = aMember.groupId();
        this.memberId = aMember.userId();
        this.createdOn = aMember.createdOn();
        this.state = aMember.state();
        this.role = aMember.role();
        this.memberIds = aMemberIds;
        this.occurredOn = aOccurredOn;
    }

    @Override
    public String streamId() {
        return groupId.qualifiedValue();
    }

    public GroupId groupId() {
        return groupId;
    }

    public UserId memberId() {
        return memberId;
    }

    public GroupMember.State state() {
        return state;
    }

    public RoleName role() {
        return role;
    }

    public Set<UserId> memberIds() {
        return memberIds;
    }

    public Date createdOn() {
        return createdOn;
    }

    @Override
    public Date occurredOn() {
        return occurredOn;
    }

    @Override
    public DomainEventType type() {
        return eventType;
    }
}
