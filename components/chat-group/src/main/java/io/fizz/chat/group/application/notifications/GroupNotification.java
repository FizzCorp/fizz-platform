package io.fizz.chat.group.application.notifications;


import io.fizz.chat.group.domain.group.GroupId;
import io.fizz.common.Utils;
import io.fizz.common.domain.ApplicationId;

public abstract class GroupNotification {
    public enum Type {
        GROUP_UPDATED("GRPU"),
        GROUP_MEMBER_ADDED("GRPMA"),
        GROUP_MEMBER_UPDATED("GRPMU"),
        GROUP_MEMBER_REMOVED("GRPMR");

        private final String value;

        Type(final String aValue) {
            this.value = aValue;
        }

        public String value() {
            return value;
        }
    }

    private final GroupId groupId;

    public GroupNotification(final GroupId aGroupId) {
        Utils.assertRequiredArgument(aGroupId, "invalid_group_id");

        this.groupId = aGroupId;
    }

    public ApplicationId appId() {
        return groupId.appId();
    }

    public GroupId groupId() {
        return groupId;
    }
}
