package io.fizz.chat.group.application.group;

import io.fizz.chat.group.domain.group.GroupMember;
import io.fizz.chataccess.domain.role.RoleName;
import io.fizz.common.domain.UserId;

import java.util.Objects;

public class GroupMemberData {
    private final UserId id;
    private final RoleName role;
    private final GroupMember.State state;

    public GroupMemberData(final UserId aId, final RoleName aRole, final GroupMember.State aState) {
        this.id = aId;
        this.role = aRole;
        this.state = aState;
    }

    public UserId id() {
        return id;
    }

    public RoleName role() {
        return role;
    }

    public GroupMember.State state() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupMemberData that = (GroupMemberData) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
