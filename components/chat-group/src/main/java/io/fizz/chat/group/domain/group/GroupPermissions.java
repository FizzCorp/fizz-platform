package io.fizz.chat.group.domain.group;

public enum GroupPermissions {
    CREATE("groupCreate"),
    EDIT_GROUP_PROFILE("groupEditProfile"),
    EDIT_MEMBER_ROLES("groupEditMemberRoles"),
    INVITE_MEMBERS("groupInviteMembers"),
    ADD_MEMBERS("groupAddMembers"),
    REMOVE_ANY_MEMBER("groupRemoveAnyMember"),
    REMOVE_SELF("groupRemoveSelf"),
    ACCEPT_MEMBERSHIP("groupAcceptMembership");

    private final String value;
    GroupPermissions(String aValue) {
        value = aValue;
    }

    public String value() {
        return value;
    }
}
