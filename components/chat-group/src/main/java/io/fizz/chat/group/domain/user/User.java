package io.fizz.chat.group.domain.user;

import io.fizz.chat.group.domain.group.*;
import io.fizz.chataccess.domain.role.Role;
import io.fizz.common.Utils;
import io.fizz.common.domain.UserId;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class User {
    private final UserId id;
    private final Role role;

    public User(final UserId aId, final Role aRole) {
        Utils.assertRequiredArgument(aId, "invalid_user_id");
        Utils.assertRequiredArgument(aRole, "invalid_user_role");

        this.id = aId;
        this.role = aRole;
    }

    public UserId id() {
        return id;
    }

    public Role role() {
        return role;
    }

    public Group create(final GroupId aGroupId, final GroupProfile aProfile, Set<GroupMember> aMembers) {
        if (!role.has(GroupPermissions.CREATE.value())) {
            throw new SecurityException("not_permitted");
        }

        return new Group(aGroupId, aProfile, aMembers, new Date());
    }
}
