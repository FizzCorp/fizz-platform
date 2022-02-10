package io.fizz.chat.group.domain.group;

import io.fizz.chatcommon.domain.events.AbstractDomainEvent;
import io.fizz.chatcommon.domain.events.DomainEventType;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.Set;

public class GroupProfileUpdated implements AbstractDomainEvent {
    public static final DomainEventType TYPE = new DomainEventType("groupProfileUpdated", GroupProfileUpdated.class);

    private final GroupId groupId;
    private final Set<UserId> members;
    private final String title;
    private final String imageURL;
    private final String description;
    private final String groupType;
    private final Date occurredOn;

    public GroupProfileUpdated(final GroupId aGroupId,
                               final Set<UserId> aMembers,
                               final String aTitle,
                               final String aImageURL,
                               final String aDescription,
                               final String aGroupType,
                               final Date aOccurredOn) {
        Utils.assertRequiredArgument(aGroupId, "invalid group id");
        Utils.assertRequiredArgument(aMembers, "invalid members");
        Utils.assertRequiredArgument(aOccurredOn, "invalid event occurred time");

        this.groupId = aGroupId;
        this.members = aMembers;
        this.title = aTitle;
        this.imageURL = aImageURL;
        this.description = aDescription;
        this.groupType = aGroupType;
        this.occurredOn = aOccurredOn;
    }

    @Override
    public String streamId() {
        return groupId.qualifiedValue();
    }

    public GroupId groupId() {
        return groupId;
    }

    public Set<UserId> members() {
        return members;
    }

    public String title() {
        return title;
    }

    public String imageURL() {
        return imageURL;
    }

    public String description() {
        return description;
    }

    public String groupType() {
        return groupType;
    }

    @Override
    public Date occurredOn() {
        return occurredOn;
    }

    @Override
    public DomainEventType type() {
        return TYPE;
    }
}
