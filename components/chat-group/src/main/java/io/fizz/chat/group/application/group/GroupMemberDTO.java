package io.fizz.chat.group.application.group;

import io.fizz.chat.group.domain.group.GroupMember;
import io.fizz.common.Utils;

import java.util.Date;

public class GroupMemberDTO {
    private final String userId;
    private final String role;
    private final String state;
    private final Date createdOn;

    public GroupMemberDTO(final GroupMember aMember) {
        Utils.assertRequiredArgument(aMember, "invalid_member");

        userId = aMember.userId().value();
        role = aMember.role().value();
        state = aMember.state().value();
        createdOn = aMember.createdOn();
    }

    public String userId() {
        return userId;
    }

    public String role() {
        return role;
    }

    public String state() {
        return state;
    }

    public Date createdOn() {
        return createdOn;
    }
}
