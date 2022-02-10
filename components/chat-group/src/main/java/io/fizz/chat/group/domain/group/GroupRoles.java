package io.fizz.chat.group.domain.group;

import io.fizz.chat.domain.Permissions;
import io.fizz.chataccess.domain.role.RoleName;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GroupRoles {
    static final Set<String> PERMISSIONS_MEMBER = new HashSet<>(
            Arrays.asList(
                    Permissions.READ_MESSAGES.value(),
                    Permissions.PUBLISH_MESSAGES.value(),
                    Permissions.EDIT_OWN_MESSAGE.value(),
                    Permissions.DELETE_OWN_MESSAGE.value(),
                    GroupPermissions.REMOVE_SELF.value(),
                    GroupPermissions.ACCEPT_MEMBERSHIP.value()
            )
    );

    static final Set<String> PERMISSIONS_MODERATOR = new HashSet<String>() {
        {
            addAll(PERMISSIONS_MEMBER);
            addAll(
                    new HashSet<>(Arrays.asList(
                            Permissions.DELETE_ANY_MESSAGE.value(),
                            Permissions.MANAGE_MUTES.value(),
                            Permissions.MANAGE_BANS.value()
                    ))
            );
        }
    };

    static final Set<String> PERMISSIONS_OWNER = new HashSet<String>() {
        {
            addAll(PERMISSIONS_MODERATOR);
            addAll(
                    new HashSet<>(Arrays.asList(
                            GroupPermissions.EDIT_GROUP_PROFILE.value(),
                            GroupPermissions.EDIT_MEMBER_ROLES.value(),
                            GroupPermissions.INVITE_MEMBERS.value(),
                            GroupPermissions.ADD_MEMBERS.value(),
                            GroupPermissions.REMOVE_ANY_MEMBER.value()
                    ))
            );
        }
    };

    static final RoleName ROLE_NAME_OWNER = new RoleName("owner");
    static final RoleName ROLE_NAME_MODERATOR = new RoleName("moderator");
    static final RoleName ROLE_NAME_MEMBER = new RoleName("member");

    static String[]
    permissionsOwner() {
        return PERMISSIONS_OWNER.toArray(new String[0]);
    }

    static String[] permissionsMod() {
        return PERMISSIONS_MODERATOR.toArray(new String[0]);
    }

    static String[] permissionsMember() {
        return PERMISSIONS_MEMBER.toArray(new String[0]);
    }
}
