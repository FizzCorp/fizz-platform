package io.fizz.chat.group.application.query;

import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.chat.group.domain.group.GroupMember;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.common.domain.UserId;

import java.util.Date;

public class UserGroupDTO {
    private final UserId userId;
    private final GroupId groupId;
    private final GroupMember.State state;
    private final RoleName role;
    private final Long lastReadMessageId;
    private final Date createdOn;

    public UserGroupDTO(final UserId aUserId,
                        final GroupId aGroupId,
                        final GroupMember.State aState,
                        final RoleName aRole,
                        final Long aLastReadMessageId,
                        final Date aCreatedOn) {
        this.userId = aUserId;
        this.groupId = aGroupId;
        this.state = aState;
        this.role = aRole;
        this.lastReadMessageId = aLastReadMessageId;
        this.createdOn = aCreatedOn;
    }

    public UserId userId() {
        return userId;
    }

    public GroupId groupId() {
        return groupId;
    }

    public GroupMember.State state() {
        return state;
    }

    public RoleName role() {
        return role;
    }

    public Long lastReadMessageId() {
        return lastReadMessageId;
    }

    public Date createdOn() {
        return createdOn;
    }
}
